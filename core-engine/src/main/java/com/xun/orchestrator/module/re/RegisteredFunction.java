package com.xun.orchestrator.module.re;

import com.xun.sdk.annotation.AiFunction;
import lombok.Getter;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module.re
 * @Author: xun
 * @CreateTime: 2025-09-12  13:46
 * @Description: TODO
 * @Version: 1.0
 */
@Getter
public class RegisteredFunction {

    private final String moduleId;
    private final String intentId;
    private final AiFunction annotation;
    private final java.lang.reflect.Method method;
    private final Object instance;

    public RegisteredFunction(String moduleId, AiFunction annotation,
                              java.lang.reflect.Method method, Object instance) {
        this.moduleId = moduleId;
        this.annotation = annotation;
        this.method = method;
        this.instance = instance;
        this.intentId = annotation.intentId();
    }

}
