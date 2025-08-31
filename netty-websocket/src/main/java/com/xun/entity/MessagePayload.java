package com.xun.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.entity
 * @Author: xun
 * @CreateTime: 2025-08-18  21:42
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagePayload {
    private String type;
    private String userId; // user_id → userId
    private String sessionId; // session_id → sessionId

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant timestamp;

    private List<ChatMessage> chatHistory; // chat_history → chatHistory
    private String currentQuery; // current_query → currentQuery

    @JsonProperty("available_tools")
    private List<Tool> availableTools;

    private Map<String, Object> context;
}
