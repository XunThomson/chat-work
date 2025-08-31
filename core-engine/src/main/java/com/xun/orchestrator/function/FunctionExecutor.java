package com.xun.orchestrator.function;

import com.xun.orchestrator.module.ModuleRegistry;
import com.xun.orchestrator.security.SandboxExecutor;
import com.xun.sdk.model.AiFunctionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.function
 * @Author: xun
 * @CreateTime: 2025-08-23  21:13
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class FunctionExecutor {
    @Autowired
    private ModuleRegistry registry;

    @Autowired
    private SandboxExecutor sandboxExecutor;

    public AiFunctionResult execute(String intentId, Map<String, Object> params, String userId) {
        ModuleRegistry.RegisteredFunction func = registry.getFunction(intentId);
        if (func == null) {
            return AiFunctionResult.failure("NOT_FOUND", "功能未注册: " + intentId);
        }

        try {
            // 沙箱执行
            return (AiFunctionResult) sandboxExecutor.executeInSandbox(
                    func.getInstance(),
                    func.getMethod(),
                    new Object[]{}
            );
        } catch (Exception e) {
            return AiFunctionResult.failure("EXEC_ERROR", e.getMessage());
        }
    }
}
