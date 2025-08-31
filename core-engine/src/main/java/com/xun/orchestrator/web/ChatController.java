package com.xun.orchestrator.web;

import com.xun.orchestrator.entity.ChatResponse;
import com.xun.orchestrator.entity.ChatSession;
import com.xun.orchestrator.function.FunctionExecutor;
import com.xun.orchestrator.intent.IntentRecognizer;
import com.xun.orchestrator.user.UserFamiliarityService;
import com.xun.orchestrator.session.UserSessionManager;
import com.xun.sdk.model.AiFunctionResult;
import com.xun.orchestrator.entity.RecognizedIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.web
 * @Author: xun
 * @CreateTime: 2025-08-23  21:17
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired private IntentRecognizer recognizer;
    @Autowired private FunctionExecutor executor;
    @Autowired private UserSessionManager sessionManager;
    @Autowired private UserFamiliarityService familiarityService;

    @PostMapping
    public String chat() {
        String userId = "user";
        String input = "hello";

        ChatSession session = sessionManager.getOrCreate(userId);
        RecognizedIntent intent = recognizer.recognize(input, userId);

        if (!intent.isConfident(0.3)) { // 使用置信度阈值
            return "我还不太明白，您可以试试说：请三天假、我上班了";
        }

        AiFunctionResult result = executor.execute(intent.getIntentId(), intent.getParameters(), userId);
        familiarityService.updateFamiliarity(session, intent.getIntentId());

        return "";
    }

    private String formatResponse(AiFunctionResult result, ChatSession session) {
        if (!result.isSuccess()) {
            return "❌ " + result.getMessage();
        }
        return "✅ " + result.getMessage() + "\n📝 已记录到您的操作历史。";
    }

    static class ChatRequest {
        private String userId, message;
        // getters & setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
