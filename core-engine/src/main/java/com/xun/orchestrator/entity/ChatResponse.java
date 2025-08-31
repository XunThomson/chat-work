package com.xun.orchestrator.entity;

import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.entity
 * @Author: xun
 * @CreateTime: 2025-08-23  22:10
 * @Description: TODO
 * @Version: 1.0
 */
public class ChatResponse {
    private String reply;
    private String intentId;
    private double confidence;
    private Map<String, Object> metadata;

    // 构造函数 & Getters & Setters
    public ChatResponse(String reply) {
        this.reply = reply;
    }

    // 静态工厂方法
    public static ChatResponse success(String reply) {
        ChatResponse res = new ChatResponse(reply);
        res.confidence = 1.0;
        return res;
    }

    public static ChatResponse withIntent(String reply, String intentId, double confidence) {
        ChatResponse res = new ChatResponse(reply);
        res.intentId = intentId;
        res.confidence = confidence;
        return res;
    }
}