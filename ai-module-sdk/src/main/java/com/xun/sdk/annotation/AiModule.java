package com.xun.sdk.annotation;

import java.lang.annotation.*;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.sdk.annotation
 * @Author: xun
 * @CreateTime: 2025-08-23  21:05
 * @Description: TODO 标记一个类为 AI 功能模块
 * @Version: 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiModule {
    String id();
    String name();
    String version() default "1.0.0";
    String description() default "";
}
