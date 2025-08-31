package com.xun.handler;

import com.xun.utils.ChannelUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.handler
 * @Author: xun
 * @CreateTime: 2025-08-16  21:51
 * @Description: TODO
 * @Version: 1.0
 */
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Resource
    private TextWebSocketFrameHandler textHandler;
    @Resource
    private HeartbeatHandler heartbeatHandler;

    @Resource
    private ChannelUtils channelUtils;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            textHandler.handle(ctx, (WebSocketFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        if ("/ws".equals(req.uri())) {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    "ws://" + req.headers().get(HttpHeaderNames.HOST) + "/ws", null, true);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }
        } else {
            // 非 WebSocket 请求，返回 404
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            heartbeatHandler.onIdle(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Channel 异常: {}", ctx.channel().id(), cause);
        channelUtils.closeChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channelUtils.unbindUser(ctx.channel());
        log.info("客户端断开: {}", ctx.channel().id());
    }
}
