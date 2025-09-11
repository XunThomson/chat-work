package com.xun.orchestrator.module.mi;

import com.xun.orchestrator.module.ModuleInstance;
import lombok.Getter;
import org.osgi.framework.Bundle;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module.mi
 * @Author: xun
 * @CreateTime: 2025-08-31  11:25
 * @Description: TODO
 * @Version: 1.0
 */
@Getter
public class LocalModuleInstance extends ModuleInstance {
    // 本地模块用
    private final Bundle bundle;
    // 本地实例
    private final Object instance;

    public LocalModuleInstance(String moduleId, Bundle bundle, Object instance) {
        super(moduleId);
        this.bundle = bundle;
        this.instance = instance;
    }
}
