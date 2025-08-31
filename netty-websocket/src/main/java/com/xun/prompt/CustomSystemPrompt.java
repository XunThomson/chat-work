package com.xun.prompt;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.Map;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.prompt
 * @Author: xun
 * @CreateTime: 2025-08-23  11:01
 * @Description: TODO
 * @Version: 1.0
 */
public class CustomSystemPrompt {

    String systemText = """
            严格符合角色定位的基础下进行:
            根据用户对话
            
            """;

    SystemPromptTemplate systemPromptTemplate;

    Message defaultChatRolePrompt(){
        systemPromptTemplate = new SystemPromptTemplate(systemText);
        return systemPromptTemplate.createMessage();
    }

}
