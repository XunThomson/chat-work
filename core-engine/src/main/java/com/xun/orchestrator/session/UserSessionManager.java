package com.xun.orchestrator.session;

import com.xun.orchestrator.entity.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.session
 * @Author: xun
 * @CreateTime: 2025-08-23  21:15
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class UserSessionManager {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "session:";

    public ChatSession getOrCreate(String userId) {
        String key = PREFIX + userId;
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj instanceof ChatSession) {
            return (ChatSession) obj;
        }
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        save(session);
        return session;
    }

    public void save(ChatSession session) {
        String key = PREFIX + session.getUserId();
        redisTemplate.opsForValue().set(key, session, Duration.ofHours(2));
    }
}
