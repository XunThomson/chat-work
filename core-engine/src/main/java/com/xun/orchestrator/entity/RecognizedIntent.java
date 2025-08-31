package com.xun.orchestrator.entity;

import java.util.Map;
import java.util.Objects;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.entity
 * @Author: xun
 * @CreateTime: 2025-08-23  21:57
 * @Description: TODO 已识别的意图
 * @Version: 1.0
 */
public class RecognizedIntent {

    private final String intentId;
    private final double confidence;
    private final Map<String, Object> parameters;
    private final String originalInput;
    private final String matchedUtterance;

    public RecognizedIntent(String intentId, double confidence, Map<String, Object> parameters,
                            String originalInput, String matchedUtterance) {
        this.intentId = Objects.requireNonNull(intentId, "intentId cannot be null");
        this.confidence = confidence;
        this.parameters = parameters;
        this.originalInput = originalInput;
        this.matchedUtterance = matchedUtterance;
    }

    // ------- Getters -------
    public String getIntentId() { return intentId; }
    public double getConfidence() { return confidence; }
    public Map<String, Object> getParameters() { return parameters; }
    public String getOriginalInput() { return originalInput; }
    public String getMatchedUtterance() { return matchedUtterance; }

    // ------- 工具方法 -------
    public boolean isConfident(double threshold) {
        return confidence >= threshold;
    }

    public boolean hasParameter(String paramName) {
        return parameters != null && parameters.containsKey(paramName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, T defaultValue) {
        if (parameters == null) return defaultValue;
        return (T) parameters.getOrDefault(key, defaultValue);
    }

    // ------- 静态工厂方法 -------
    public static RecognizedIntent notMatched() {
        return new Builder()
                .intentId("unknown")
                .confidence(0.0)
                .originalInput("")
                .build();
    }

    public static RecognizedIntent unknown(String input) {
        return new Builder()
                .intentId("unknown")
                .confidence(0.0)
                .originalInput(input)
                .build();
    }

    // ------- Builder -------
    public static class Builder {
        private String intentId;
        private double confidence = 1.0;
        private Map<String, Object> parameters;
        private String originalInput;
        private String matchedUtterance;

        public Builder intentId(String intentId) {
            this.intentId = intentId;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder originalInput(String input) {
            this.originalInput = input;
            return this;
        }

        public Builder matchedUtterance(String utterance) {
            this.matchedUtterance = utterance;
            return this;
        }

        public RecognizedIntent build() {
            return new RecognizedIntent(intentId, confidence, parameters, originalInput, matchedUtterance);
        }
    }

    // ------- Object 方法 -------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecognizedIntent that = (RecognizedIntent) o;
        return Double.compare(that.confidence, confidence) == 0 &&
                Objects.equals(intentId, that.intentId) &&
                Objects.equals(parameters, that.parameters) &&
                Objects.equals(originalInput, that.originalInput) &&
                Objects.equals(matchedUtterance, that.matchedUtterance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intentId, confidence, parameters, originalInput, matchedUtterance);
    }

    @Override
    public String toString() {
        return "RecognizedIntent{" +
                "intentId='" + intentId + '\'' +
                ", confidence=" + String.format("%.3f", confidence) +
                ", parameters=" + parameters +
                ", originalInput='" + originalInput + '\'' +
                ", matchedUtterance='" + matchedUtterance + '\'' +
                '}';
    }
}