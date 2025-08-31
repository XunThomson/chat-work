package com.xun.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun.constant.RedisKeys;
import com.xun.entity.ChatMessage;
import com.xun.entity.MessagePayload;
import com.xun.entity.MessageResponse;
import com.xun.entity.Tool;
import com.xun.ollama.service.OllamaService;
import com.xun.service.ChatMessagePlusService;
import com.xun.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.service.impl
 * @Author: xun
 * @CreateTime: 2025-08-19  12:51
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessagePlusServiceImpl implements ChatMessagePlusService {

    private final ObjectMapper objectMapper;
    private final ChannelUtils channelUtils;
    private final OllamaService ollamaService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String BIND_TYPE = "BIND";
    private static final String ERROR_TYPE = "ERROR";
    private static final String DELIMITER_START = "<tool_call>";
    private static final String DELIMITER_END = "</tool_call>";

    @Value("${spring.ai.ollama.chat.model}")
    private String aiName;

    /**
     * 主入口：处理 WebSocket 消息
     */
    @Override
    public void handleChatMessage(Channel channel, String payload) {
        try {
            // 1. 解析并验证请求
            MessagePayload request = parseAndValidatePayload(payload, channel)
                    .orElse(null);


            if (request == null) {
                return; // 已发送错误
            }



            // 2. 调用 AI 服务
            String aiRawResponse;
            try {
//                aiRawResponse = ollamaService.getAskStrResultPlus(payload);
                aiRawResponse = "<tool_call>{\"name\": \"query_service_status\", \"arguments\": {\"service_name\": \"*\", \"user_id\": \"u123\"}}</tool_call>\n";
            } catch (Exception e) {
                log.error("调用 Ollama 服务失败", e);
                sendError(channel, "AI 服务暂时不可用");
                return;
            }

            // 3. 清理响应（掐头去尾）
            String cleanedResponse = extractContent(aiRawResponse);


            // 4. 判断是否需要执行工具（如 query_service_status）
            if (isToolCallRequest(cleanedResponse)) {
                handleToolCall(cleanedResponse, request);
                return;
            }

            // 5. 正常消息：转发给用户（通过 Redis）
            publishToUser(request.getUserId(), payload);

        } catch (Exception e) {
            log.error("处理消息时发生未预期错误", e);
            sendError(channel, "系统内部错误");
        }
    }


    // ================== 解析与验证 ==================

    private Optional<MessagePayload> parseAndValidatePayload(String payload, Channel channel) {
        JsonNode root;
        try {
            root = objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            log.warn("无效的 JSON 格式", e);
            sendError(channel, "消息格式错误");
            return Optional.empty();
        }

        String userId = getTextNode(root, "user_id");
        if (userId == null) {
            sendError(channel, "缺少 user_id");
            return Optional.empty();
        }

        // 检查用户是否已绑定
        if (!isUserBound(channel, userId)) {
            String type = getTextNode(root, "type");
            if (type == null || !BIND_TYPE.equals(type)) {
                sendError(channel, "请先绑定身份");
                return Optional.empty();
            }
        }

        return parseMessagePayload(root);
    }

    private Optional<MessagePayload> parseMessagePayload(JsonNode root) {
        try {
            String type = getTextNode(root, "type");
            String userId = getTextNode(root, "user_id");
            String sessionId = getTextNode(root, "session_id");
            String currentQuery = getTextNode(root, "current_query");

            if (userId == null) {
                return Optional.empty();
            }

            // 解析 timestamp（可选字段）
            Instant timestamp = null;
            String tsStr = getTextNode(root, "timestamp");
            if (tsStr != null && !tsStr.isEmpty()) {
                try {
                    timestamp = Instant.parse(tsStr);
                } catch (DateTimeParseException e) {
                    log.warn("时间戳格式错误: {}", tsStr, e);
                    return Optional.empty();
                }
            }

            // 解析 available_tools（数组）
            List<Tool> availableTools = null;
            if (root.has("available_tools")) {
                JsonNode toolsNode = root.get("available_tools");
                if (toolsNode.isArray()) {
                    try {
                        availableTools = objectMapper.convertValue(toolsNode, new TypeReference<List<Tool>>() {
                        });
                    } catch (Exception e) {
                        log.warn("解析 available_tools 失败", e);
                    }
                }
            }

            // 解析 context（对象）
            Map<String, Object> context = null;
            if (root.has("context")) {
                try {
                    context = objectMapper.convertValue(root.get("context"), new TypeReference<Map<String, Object>>() {
                    });
                } catch (Exception e) {
                    log.warn("解析 context 失败", e);
                }
            }

            // 构建并返回
            MessagePayload payload = new MessagePayload(
                    type,
                    userId,
                    sessionId,
                    timestamp,
                    null, // chatHistory 后续填充
                    currentQuery,
                    availableTools,
                    context
            );

            return Optional.of(payload);

        } catch (Exception e) {
            log.error("构建 MessagePayload 时发生错误", e);
            return Optional.empty();
        }
    }

    private boolean isUserBound(Channel channel, String userId) {
        String boundId = channelUtils.getUserId(channel);
        return boundId != null && boundId.equals(userId);
    }

    private String getTextNode(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : null;
    }

    // ================== 响应处理 ==================

    private String cleanDelimitedResponse(String response) {
        if (response == null || response.length() < 2) return response;
        if (response.startsWith(DELIMITER_START) && response.endsWith(DELIMITER_END)) {
            return response.substring(1, response.length() - 1);
        }
        return response;
    }

    public static String extractContent(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        int startIndex = input.indexOf(DELIMITER_START);
        int endIndex = input.lastIndexOf(DELIMITER_END);

        // 检查是否都存在
        if (startIndex == -1 || endIndex == -1) {
            return null; // 分隔符不存在
        }

        // 开始位置后移，跳过分隔符
        startIndex += DELIMITER_START.length();

        // 确保顺序正确：开始在前，结束在后
        if (startIndex >= endIndex) {
            return null; // 顺序错误，如 "<tool_call><tool_call>" 或 "<tool_call>hello<tool_call>" 但 startIndex >= endIndex
        }

        return input.substring(startIndex, endIndex);
    }

    private boolean isToolCallRequest(String cleanedResponse) {
        // 简单判断：是否以 {"name": 开头（实际可更精确）
        return cleanedResponse != null && cleanedResponse.trim().startsWith("{\"name\"");
    }

    // ================== 工具调用处理（示例）==================

    private void handleToolCall(String toolCallJson, MessagePayload request) {
        try {
            JsonNode toolNode = objectMapper.readTree(toolCallJson);
            String toolName = getTextNode(toolNode, "name");
            JsonNode arguments = toolNode.get("arguments");

            switch (toolName) {
                case "query_service_status":
                    handleQueryServiceStatus(arguments, request.getUserId(), request.getSessionId());
                    break;
                case "execute_function":
                    handleExecuteFunction(arguments, request.getUserId());
                    break;
                default:
                    log.warn("未知工具调用: {}", toolName);
            }
        } catch (Exception e) {
            log.error("处理工具调用失败", e);
        }
    }

    private void handleQueryServiceStatus(JsonNode args, String userId, String sessionId) {
//        try {
//            // 示例：你可以调用真正的服务
//            log.info("用户 {} 查询服务状态: {}", userId, args);
//            // TODO: 实际查询逻辑
//
//            sendToolResultToModel(userId, sessionId, "query_service_status", resultJson); // 送回模型
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }

    }

    private void handleExecuteFunction(JsonNode args, String userId) {
        log.info("用户 {} 执行函数: {}", userId, args);
        // TODO: 执行函数逻辑
    }

    private void sendToolResultToModel(String userId, String chatId, String toolName, String resultJson) {
        try {

            // 添加工具返回结果
//            //添加用户msg
//            AiChatMessage aiChatMessage = saveMsg(
//                    Long.valueOf(userId),
//                    resultJson,
//                    chatId,
//                    LocalDateTime.now(),
//                    "ai"
//            );
//
//            // 使用 chatId 查询当前会话的消息历史
//            List<AiChatMessage> history = aiChatMessageService.findTopNByChatIdOrderByCreatedAtDesc(chatId, 10);

            // 调用大模型生成回复
//            String finalReply = ollamaService.chat(history);

            // 发送给用户
//            publishToUser(userId, finalReply);

        } catch (Exception e) {
            log.error("处理工具返回失败, userId={}, chatId={}", userId, chatId, e);
            publishToUser(userId, "抱歉，服务暂时不可用。");
        }
    }

    // ================== 消息发送 ==================

    private void sendError(Channel channel, String message) {
        String errorMsg = new MessageResponse(ERROR_TYPE, message).toString();
        channel.writeAndFlush(new TextWebSocketFrame(errorMsg));
    }

    private void publishToUser(String userId, String payload) {
        stringRedisTemplate.convertAndSend(RedisKeys.TOPIC_PRIVATE_PLUS_MSG, payload);
    }

}
