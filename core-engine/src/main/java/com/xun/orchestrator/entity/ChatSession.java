package com.xun.orchestrator.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.entity
 * @Author: xun
 * @CreateTime: 2025-08-23  21:41
 * @Description: TODO
 * @Version: 1.0
 */
public class ChatSession {
    private String userId;
    private String currentIntent;
    private Map<String, Object> params = new HashMap<>();
    private long lastActiveTime = System.currentTimeMillis();
    private UserFamiliarityLevel familiarityLevel = UserFamiliarityLevel.STRANGER;
    private List<String> knownFunctions = new ArrayList<>();
    private Map<String, Double> utteranceWeights = new HashMap<>();
    private String preferredName;

    public enum UserFamiliarityLevel {
        STRANGER, NEWBIE, REGULAR, EXPERT
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrentIntent() {
        return currentIntent;
    }

    public void setCurrentIntent(String currentIntent) {
        this.currentIntent = currentIntent;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public UserFamiliarityLevel getFamiliarityLevel() {
        return familiarityLevel;
    }

    public void setFamiliarityLevel(UserFamiliarityLevel familiarityLevel) {
        this.familiarityLevel = familiarityLevel;
    }

    public List<String> getKnownFunctions() {
        return knownFunctions;
    }

    public void setKnownFunctions(List<String> knownFunctions) {
        this.knownFunctions = knownFunctions;
    }

    public Map<String, Double> getUtteranceWeights() {
        return utteranceWeights;
    }

    public void setUtteranceWeights(Map<String, Double> utteranceWeights) {
        this.utteranceWeights = utteranceWeights;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }
}
