package com.xun.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun.entity.MutualMessagePayload;
import com.xun.service.MessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.handler
 * @Author: xun
 * @CreateTime: 2025-08-16  21:52
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TextWebSocketFrameHandler {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private MessageService messageService;

    public void handle(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            try {

                JsonNode root = objectMapper.readTree(text);

                // 提取 type 字段
                String type = root.has("type") ? root.get("type").asText() : null;
                if (type == null) {
                    return;
                }

                switch (type){
                    case "BIND":
                    case "PING":
                    case "MESSAGE":
                        MutualMessagePayload payload = objectMapper.readValue(text, MutualMessagePayload.class);
                        messageService.handleMessage(ctx.channel(), payload);
                        break;
                    case "MESSAGE_PLUS":
                        messageService.handleChatMessage(ctx.channel(), text);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.warn("消息解析失败: {}", text, e);
                ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"error\":\"invalid_message\"}"));
            }
        }
    }
}
