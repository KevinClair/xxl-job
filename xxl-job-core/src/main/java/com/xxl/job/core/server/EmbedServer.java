package com.xxl.job.core.server;

import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.impl.ExecutorBizImpl;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.server.handler.EmbedHttpServerHandler;
import com.xxl.job.core.thread.ExecutorRegistryThread;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.ThrowableUtil;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * Copy from : https://github.com/xuxueli/xxl-rpc
 *
 * @author xuxueli 2020-04-11 21:25
 */
public class EmbedServer implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(EmbedServer.class);

    private final ExecutorBiz executorBiz;

    private final XxlJobConfiguration configuration;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private Channel channel;

    public EmbedServer(final ExecutorBiz executorBiz, final XxlJobConfiguration configuration) {
        this.executorBiz = executorBiz;
        this.configuration = configuration;
        try {
            // start server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                            .addLast(new IdleStateHandler(0, 0, 30 * 3, TimeUnit.SECONDS))  // beat 3N, close if idle
                            .addLast(new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(5 * 1024 * 1024))  // merge request & reponse to FULL
                            .addLast(new EmbedHttpServerHandler(executorBiz, configuration.getAccessToken()));
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            // bind
            channel = bootstrap.bind(configuration.getPort()).sync().channel();

            logger.info(">>>>>>>>>>> xxl-job remoting server start success, nettype = {}, port = {}", EmbedServer.class, configuration.getPort());

            // start registry
            startRegistry(configuration.getAppName(), configuration.getAddress());

        } catch (InterruptedException e) {
            logger.info(">>>>>>>>>>> xxl-job remoting server stop.");
        } catch (Exception e) {
            logger.error(">>>>>>>>>>> xxl-job remoting server error.", e);
        }

    }

    @Override
    public void destroy() throws Exception {
        // close server
        if (Objects.nonNull(channel)){
            channel.close().sync();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        // stop registry
        stopRegistry();
        logger.info(">>>>>>>>>>> xxl-job remoting server destroy success.");
    }

    // ---------------------- registry ----------------------

    public void startRegistry(final String appname, final String address) {
        // start registry
        ExecutorRegistryThread.getInstance().start(appname, address);
    }

    public void stopRegistry() {
        // stop registry
        ExecutorRegistryThread.getInstance().toStop();
    }
}
