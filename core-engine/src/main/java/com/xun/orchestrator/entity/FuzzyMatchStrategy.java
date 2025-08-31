package com.xun.orchestrator.entity;

import org.springframework.stereotype.Component;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.entity
 * @Author: xun
 * @CreateTime: 2025-08-23  22:10
 * @Description: TODO 模糊匹配
 * @Version: 1.0
 */
@Component
public class FuzzyMatchStrategy implements MatchStrategy {
    @Override
    public boolean matches(String input, String utterance) {
        return fuzzyContains(input, utterance);
    }

    @Override
    public double confidence(String input, String utterance) {
        // 可基于匹配长度、关键词等计算
        return 0.8 + Math.random() * 0.2;
    }

    private boolean fuzzyContains(String input, String utterance) {
        input = input.toLowerCase().replaceAll("[\\s？]", "");
        utterance = utterance.toLowerCase().replaceAll("[\\s？]", "");
        return input.contains(utterance);
    }
}
