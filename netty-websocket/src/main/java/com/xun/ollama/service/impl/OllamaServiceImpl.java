package com.xun.ollama.service.impl;

import com.xun.ollama.service.OllamaService;
import com.xun.utils.AiVisitDaoTools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.ollama.service.impl
 * @Author: xun
 * @CreateTime: 2025-07-30  14:09
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class OllamaServiceImpl implements OllamaService {

    private final ChatClient chatClient;

    public OllamaServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    @Override
    public Flux<String> getAskResult(String msg) {
        return null;
    }

    @Override
    public String getAskStrResult(String msg) {
        return "";
    }

    @Override
    public String getAskStrResultPlus(String msg) {
        return chatClient
                .prompt()
                .user(msg)
                .tools(new AiVisitDaoTools())
                .call()
                .content();
    }

//    @Override
//    public String getAskStrResultPlus(List<AiChatMessage> history) {
//
//        return "";
//    }


}
