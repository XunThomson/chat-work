package com.xun.orchestrator.module;

import lombok.Data;
import lombok.Getter;
import org.osgi.framework.Bundle;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module
 * @Author: xun
 * @CreateTime: 2025-08-23  21:10
 * @Description: TODO 表示一个已加载的模块实例
 * @Version: 1.0
 */
@Getter
public abstract class ModuleInstance {
    private final String moduleId;

    private final long loadTime;

    public ModuleInstance(String moduleId) {
        this.moduleId = moduleId;
        this.loadTime = System.currentTimeMillis();
    }
}
