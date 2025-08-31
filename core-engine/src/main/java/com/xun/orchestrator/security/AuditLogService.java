package com.xun.orchestrator.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.security
 * @Author: xun
 * @CreateTime: 2025-08-23  21:15
 * @Description: TODO 审计日志服务
 * @Version: 1.0
 */
@Service
public class AuditLogService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void log(String userId, String intentId, Map<String, Object> params, String result) {
        Map<String, Object> log = new HashMap<>();
        log.put("userId", userId);
        log.put("intentId", intentId);
        log.put("params", maskParams(params)); // 脱敏
        log.put("result", result);
        log.put("timestamp", Instant.now().toEpochMilli());

        redisTemplate.opsForList().leftPush("audit:logs", log);
    }

    private Map<String, Object> maskParams(Map<String, Object> params) {
        Map<String, Object> masked = new HashMap<>(params);
        masked.replaceAll((k, v) -> "sensitive".equals(k) ? "****" : v);
        return masked;
    }
}
