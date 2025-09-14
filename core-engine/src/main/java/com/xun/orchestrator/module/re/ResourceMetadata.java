package com.xun.orchestrator.module.re;

import lombok.Getter;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module.re
 * @Author: xun
 * @CreateTime: 2025-09-13  22:09
 * @Description: TODO
 * @Version: 1.0
 */
@Getter
public class ResourceMetadata {
    private final String canonicalName; // 官方名称（注册名）
    private final Object resource;
    private final boolean allowAlias;   // 是否允许别名

    public ResourceMetadata(String canonicalName, Object resource, boolean allowAlias) {
        this.canonicalName = canonicalName;
        this.resource = resource;
        this.allowAlias = allowAlias;
    }
}
