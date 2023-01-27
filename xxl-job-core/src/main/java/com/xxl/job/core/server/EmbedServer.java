package com.xxl.job.core.server;

import com.xxl.job.common.service.ExecutorManager;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.server.handler.EmbedHttpServerHandler;
import com.xxl.job.core.thread.ExecutorRegistryThread;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Copy from : https://github.com/xuxueli/xxl-rpc
 *
 * @author xuxueli 2020-04-11 21:25
 */
public class EmbedServer {
    private static final Logger logger = LoggerFactory.getLogger(EmbedServer.class);

    public EmbedServer(final ExecutorManager executorManager, final XxlJobConfiguration configuration, final ExecutorRegistryThread executorRegistryThread) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
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
                                    .addLast(new EmbedHttpServerHandler(executorManager, configuration.getAccessToken()));
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            // bind
            ChannelFuture channelFuture = bootstrap.bind(configuration.getPort()).sync();

            logger.info(">>>>>>>>>>> xxl-job remoting server start success, nettype = {}, port = {}", EmbedServer.class, configuration.getPort());

            // start registry
            executorRegistryThread.startRegistry();

            channelFuture.channel().closeFuture().addListener(future -> {
                // close server
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                logger.info(">>>>>>>>>>> xxl-job remoting server stop success, nettype = {}, port = {}", EmbedServer.class, configuration.getPort());
            });
        } catch (Exception e) {
            logger.error(">>>>>>>>>>> xxl-job remoting server error.", e);
        }

    }
}
