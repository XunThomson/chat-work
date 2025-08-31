package com.xun.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.utils
 * @Author: xun
 * @CreateTime: 2025-08-16  21:58
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber {

    private final ObjectMapper objectMapper;

    @Resource
    private ChannelUtils channelUtils;

    public void onMessage(String message) {
        try {
            // 每个 channel 创建新的 frame
            channelUtils.getAllLocalChannels().forEach(ch -> {
                if (ch != null && ch.isActive()) {
                    TextWebSocketFrame frame = new TextWebSocketFrame(message);
                    ch.writeAndFlush(frame);
                    // Netty 会在 writeAndFlush 后自动 release frame
                }
            });
        } catch (Exception e) {
            log.error("Redis 消息处理失败", e);
        }
    }
}
