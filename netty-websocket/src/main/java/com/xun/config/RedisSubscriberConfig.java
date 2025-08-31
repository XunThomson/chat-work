package com.xun.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun.constant.RedisKeys;
import com.xun.listener.BroadcastMessageListener;
import com.xun.listener.PrivateMessageListener;
import com.xun.listener.PrivateMessagePlusListener;
import com.xun.utils.ChannelUtils;
import com.xun.utils.RedisMessageSubscriber;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.config
 * @Author: xun
 * @CreateTime: 2025-08-17  16:41
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
@Slf4j
public class RedisSubscriberConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory,
            BroadcastMessageListener broadcastListener,
            PrivateMessageListener privateListener,
            PrivateMessagePlusListener privateMessagePlusListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);

        // 注册广播监听器
        container.addMessageListener(broadcastListener, new PatternTopic(RedisKeys.TOPIC_BROADCAST));

        // 注册私信监听器
        container.addMessageListener(privateListener, new PatternTopic(RedisKeys.TOPIC_PRIVATE_MSG));

        container.addMessageListener(privateMessagePlusListener, new PatternTopic(RedisKeys.TOPIC_PRIVATE_PLUS_MSG));

        return container;
    }
}