package com.xun.sdk.annotation;

import java.lang.annotation.*;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.sdk.annotation
 * @Author: xun
 * @CreateTime: 2025-09-13  22:16
 * @Description: TODO
 * @Version: 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SharedResource {
    /**
     * 资源名称（别名），对应 SharedResourceRegistry 中注册的 key
     */
    String value();

    /**
     * 是否可选，默认 false（required）
     */
    boolean optional() default false;
}
