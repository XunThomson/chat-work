package com.xun.orchestrator.module.mi;

import com.xun.orchestrator.module.ModuleInstance;
import lombok.Getter;
import org.osgi.framework.Bundle;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module.mi
 * @Author: xun
 * @CreateTime: 2025-08-31  11:26
 * @Description: TODO
 * @Version: 1.0
 */
@Getter
public class RemoteModuleInstance extends ModuleInstance {

    // 远程地址
    private final String serviceUrl;
    // 协议：http, grpc, dubbo 等
    private final String protocol;

    public RemoteModuleInstance(String moduleId, String serviceUrl, String protocol) {
        super(moduleId);
        this.serviceUrl = serviceUrl;
        this.protocol = protocol;
    }
}
