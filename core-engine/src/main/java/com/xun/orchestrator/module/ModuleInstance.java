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
public class ModuleInstance {
    private final String moduleId;
    // 本地模块用
    private final Bundle bundle;
    // 本地实例
    private final Object instance;
    private final long loadTime;
    // 是否是远程模块
    private final boolean isRemote;
    // 远程地址
    private final String serviceUrl;
    // 协议：http, grpc, dubbo 等
    private final String protocol;

    public ModuleInstance(String moduleId, Bundle bundle, Object instance) {
        this.moduleId = moduleId;
        this.bundle = bundle;
        this.instance = instance;
        this.loadTime = System.currentTimeMillis();
        this.isRemote = false;
        this.serviceUrl = null;
        this.protocol = null;
    }

    public ModuleInstance(String moduleId, String serviceUrl, String protocol) {
        this.moduleId = moduleId;
        this.serviceUrl = serviceUrl;
        this.protocol = protocol;
        this.loadTime = System.currentTimeMillis();
        this.isRemote = true;
        this.bundle = null;
        this.instance = null;
    }
}
