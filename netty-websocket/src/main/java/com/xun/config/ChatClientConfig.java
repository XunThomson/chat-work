package com.xun.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.config
 * @Author: xun
 * @CreateTime: 2025-08-23  09:46
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(OllamaChatModel chatModel) {

//        Resource systemPromptResource = new ClassPathResource("prompts/system-prompt.txt");

        return ChatClient.builder(chatModel)
//                .defaultSystem(systemPromptResource) // 可自定义系统提示
                .build();
    }

}
