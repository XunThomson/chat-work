package com.xun.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun.constant.RedisKeys;
import com.xun.utils.ChannelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.listener
 * @Author: xun
 * @CreateTime: 2025-08-17  19:23
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateMessageListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelUtils channelUtils;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = toString(message.getChannel());
            String body = toString(message.getBody());


            if (RedisKeys.TOPIC_PRIVATE_MSG.equals(channel)) {
                log.debug("私信 | 频道: {}, 内容: {}", channel, body);

                Map<String, String> payload = objectMapper.readValue(body, new TypeReference<Map<String, String>>() {});
                String userId = payload.get("userId");
                String content = payload.get("content");

                if (userId == null || content == null) {
                    log.warn("私信消息缺少必要字段: {}", body);
                    return;
                }

                String channelId = stringRedisTemplate.opsForValue().get(RedisKeys.USER_CHANNEL + userId);
                if (channelId == null) {
                    log.debug("用户未在线: {}", userId);
                    return;
                }

                io.netty.channel.Channel nettyChannel = channelUtils.getLocalChannel(channelId);
                if (nettyChannel != null && nettyChannel.isActive()) {
                    String response = objectMapper.writeValueAsString(
                            Map.of("type", "PRIVATE", "from", "system", "content", content)
                    );
                    nettyChannel.writeAndFlush(new io.netty.handler.codec.http.websocketx.TextWebSocketFrame(response));
                    log.info("私信已发送给用户: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("处理私信消息失败", e);
        }
    }

    private String toString(byte[] bytes) {
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }
}