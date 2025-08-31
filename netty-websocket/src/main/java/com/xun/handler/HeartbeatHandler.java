package com.xun.handler;

import com.xun.utils.ChannelUtils;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.handler
 * @Author: xun
 * @CreateTime: 2025-08-16  21:53
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
public class HeartbeatHandler {

    @Resource
    ChannelUtils channelUtils;

    public void onIdle(ChannelHandlerContext ctx) {
        log.info("客户端超时，关闭连接: {}", ctx.channel().id());
        channelUtils.closeChannel(ctx.channel());
    }
}
