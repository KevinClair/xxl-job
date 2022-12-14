package com.xxl.job.core.server.handler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.IdleBeatParam;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.LogParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.server.EmbedServer;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.ThrowableUtil;
import com.xxl.job.core.util.XxlJobRemotingUtil;

/**
 * xxl-job的http服务器处理handler
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class EmbedHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(EmbedHttpServerHandler.class);

    private final ExecutorBiz executorBiz;
    private final String accessToken;
    private final ThreadPoolExecutor bizThreadPool;

    public EmbedHttpServerHandler(ExecutorBiz executorBiz, String accessToken) {
        this.executorBiz = executorBiz;
        this.accessToken = accessToken;
        this.bizThreadPool = new ThreadPoolExecutor(
            0,
            200,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(2000),
            r -> new Thread(r, "xxl-job, EmbedServer bizThreadPool-" + r.hashCode()),
            (r, executor) -> {
                throw new RuntimeException("xxl-job, EmbedServer bizThreadPool is EXHAUSTED!");
            });
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        // request parse
        String requestData = msg.content().toString(CharsetUtil.UTF_8);
        String uri = msg.uri();
        HttpMethod httpMethod = msg.method();
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        String accessTokenReq = msg.headers().get(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN);

        // invoke
        bizThreadPool.execute(() -> {
            // do invoke
            Object responseObj = process(httpMethod, uri, requestData, accessTokenReq);

            // to json
            String responseJson = GsonTool.toJson(responseObj);

            // write response
            writeResponse(ctx, keepAlive, responseJson);
        });
    }

    private Object process(HttpMethod httpMethod, String uri, String requestData, String accessTokenReq) {
        // valid
        if (HttpMethod.POST != httpMethod) {
            return ReturnT.fail("invalid request, HttpMethod not support.");

        }
        if (!StringUtils.hasLength(uri)) {
            return ReturnT.fail("invalid request, uri-mapping empty.");
        }
        if (!StringUtils.hasLength(accessTokenReq) && !accessToken.equals(accessTokenReq)) {
            return ReturnT.fail("The access token is wrong.");
        }

        // services mapping
        try {
            switch (uri) {
                case "/beat":
                    return executorBiz.beat();
                case "/idleBeat":
                    IdleBeatParam idleBeatParam = GsonTool.fromJson(requestData, IdleBeatParam.class);
                    return executorBiz.idleBeat(idleBeatParam);
                case "/run":
                    TriggerParam triggerParam = GsonTool.fromJson(requestData, TriggerParam.class);
                    return executorBiz.run(triggerParam);
                case "/kill":
                    KillParam killParam = GsonTool.fromJson(requestData, KillParam.class);
                    return executorBiz.kill(killParam);
                case "/log":
                    LogParam logParam = GsonTool.fromJson(requestData, LogParam.class);
                    return executorBiz.log(logParam);
                default:
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping(" + uri + ") not found.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ReturnT<String>(ReturnT.FAIL_CODE, "request error:" + ThrowableUtil.toString(e));
        }
    }

    /**
     * write response
     */
    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, String responseJson) {
        // write response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));   //  Unpooled.wrappedBuffer(responseJson)
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");       // HttpHeaderValues.TEXT_PLAIN.toString()
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(">>>>>>>>>>> xxl-job provider netty_http server caught exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();      // beat 3N, close if idle
            logger.debug(">>>>>>>>>>> xxl-job provider netty_http server close an idle channel.");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
