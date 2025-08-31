package com.xun.sdk.annotation;

import java.lang.annotation.*;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.sdk.annotation
 * @Author: xun
 * @CreateTime: 2025-08-23  21:06
 * @Description: TODO 标记一个方法为可被 AI 调用的功能点
 * @Version: 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiFunction {
    String intentId();
    String[] utterances() default {};
    String description() default "";
    String[] permissions() default {};
}
