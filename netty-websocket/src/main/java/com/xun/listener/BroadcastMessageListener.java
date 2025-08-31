package com.xun.listener;

import com.xun.constant.RedisKeys;
import com.xun.utils.RedisMessageSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.listener
 * @Author: xun
 * @CreateTime: 2025-08-17  19:22
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastMessageListener implements MessageListener {

    private final RedisMessageSubscriber subscriber;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = toString(message.getChannel());
            String body = toString(message.getBody());


            if (RedisKeys.TOPIC_BROADCAST.equals(channel)) { // 建议使用 RedisKeys.TOPIC_BROADCAST
                log.debug("广播 | 频道: {}, 内容: {}", channel, body);
                subscriber.onMessage(body);
            }
        } catch (Exception e) {
            log.error("处理广播消息失败", e);
        }
    }

    private String toString(byte[] bytes) {
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }
}
