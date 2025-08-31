package com.xun.sdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.sdk.model
 * @Author: xun
 * @CreateTime: 2025-08-23  21:07
 * @Description: TODO 模块执行结果统一格式
 * @Version: 1.0
 */
public class AiFunctionResult {
    private boolean success;
    private String code;
    private String message;
    private Map<String, Object> data = new HashMap<>();

    public static AiFunctionResult success() {
        return new AiFunctionResult().setSuccess(true).setCode("OK");
    }

    public static AiFunctionResult success(String message) {
        return success().setMessage(message);
    }

    public static AiFunctionResult failure(String code, String message) {
        return new AiFunctionResult()
                .setSuccess(false)
                .setCode(code)
                .setMessage(message);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public AiFunctionResult setSuccess(boolean success) { this.success = success; return this; }
    public String getCode() { return code; }
    public AiFunctionResult setCode(String code) { this.code = code; return this; }
    public String getMessage() { return message; }
    public AiFunctionResult setMessage(String message) { this.message = message; return this; }
    public Map<String, Object> getData() { return data; }
    public AiFunctionResult setData(Map<String, Object> data) { this.data = data; return this; }
    public AiFunctionResult addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
