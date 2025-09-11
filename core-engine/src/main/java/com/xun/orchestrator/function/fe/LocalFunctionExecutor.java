package com.xun.orchestrator.function.fe;

import com.xun.orchestrator.function.FunctionExecutor;
import com.xun.orchestrator.module.ModuleRegistry;
import com.xun.orchestrator.module.mi.LocalModuleInstance;
import com.xun.orchestrator.security.SandboxExecutor;
import com.xun.sdk.model.AiFunctionResult;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.function.fe
 * @Author: xun
 * @CreateTime: 2025-08-31  11:59
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Qualifier("local")
public class LocalFunctionExecutor implements FunctionExecutor {

    @Resource
    private ModuleRegistry registry;

    @Resource
    private SandboxExecutor executor;

    @Override
    public AiFunctionResult execute(String intentId,Object... args) {

        ModuleRegistry.RegisteredFunction func = registry.getFunction(intentId);
        if (func == null) {
            return AiFunctionResult.failure("NOT_FOUND", "功能未注册: " + intentId);
        }

        try {
            // 沙箱执行
            return (AiFunctionResult) executor.executeInSandbox(
                    func.getInstance(),
                    func.getMethod(),
                    args
            );
        } catch (Exception e) {
            return AiFunctionResult.failure("EXEC_ERROR", e.getMessage());
        }

    }
}
