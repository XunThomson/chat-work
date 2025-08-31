package com.xun.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun.constant.RedisKeys;
import com.xun.entity.MessagePayload;
import com.xun.entity.MutualMessagePayload;
import com.xun.ollama.service.OllamaService;
import com.xun.service.ChatMessagePlusService;
import com.xun.service.MessageService;
import com.xun.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.service.impl
 * @Author: xun
 * @CreateTime: 2025-08-16  21:56
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private RedisTemplate<String, Object> jsonRedisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private  ChannelUtils channelUtils;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private OllamaService ollamaService;

    @Resource
    private ChatMessagePlusService chatMessagePlusService;

    @Override
    public void handleMessage(Channel channel, MutualMessagePayload payload) {

        if (null == channelUtils.getUserId(channel) || !channelUtils.getUserId(channel).equals(payload.getUserId())){
            if (!"BIND".equals(payload.getType())) {
                channel.writeAndFlush(new TextWebSocketFrame(
                        "{\"type\":\"ERROR\",\"msg\":\"请先绑定身份\"}"
                ));
                return;
            }
        }

        if ("PING".equals(payload.getType())) {
            channel.writeAndFlush(new TextWebSocketFrame(
                    "{\"type\":\"ACK\",\"msg\":\"PING_SUCCESS\"}"
            ));
            return;
        }

        // 绑定用户（示例）
        if ("BIND".equals(payload.getType())) {

            String userId = payload.getUserId();
            if (userId == null || userId.trim().isEmpty()) {
                channel.writeAndFlush(new TextWebSocketFrame(
                        "{\"type\":\"ERROR\",\"msg\":\"userId 不能为空\"}"
                ));
                return;
            }

            channelUtils.bindUser(channel, userId);

            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String sessionId = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            channel.writeAndFlush(new TextWebSocketFrame("{\"type\":\"ACK\",\"msg\":\"" + sessionId +"\"}"));
            return;
        }

        if (false){
            // 广播消息（通过 Redis 发布到其他节点）
            jsonRedisTemplate.convertAndSend(RedisKeys.TOPIC_BROADCAST, payload);
        } else {
            // 发送给用户个体
            jsonRedisTemplate.convertAndSend(RedisKeys.TOPIC_PRIVATE_MSG,payload);
        }

    }

    @Override
    public void handleChatMessage(Channel channel, String payload) {

        chatMessagePlusService.handleChatMessage(channel,payload);

    }
}
