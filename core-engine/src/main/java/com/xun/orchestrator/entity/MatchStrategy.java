package com.xun.orchestrator.entity;

public interface MatchStrategy {
    boolean matches(String input, String utterance);
    double confidence(String input, String utterance); // 返回置信度
}
