package com.xun.orchestrator.security;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.security
 * @Author: xun
 * @CreateTime: 2025-08-23  21:14
 * @Description: TODO 沙盒执行器
 * @Version: 1.0
 */
@Component
public class SandboxExecutor {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Object executeInSandbox(Object instance, Method method, Object[] args) throws Exception {
        Future<Object> future = executor.submit(() -> {
            try {
                return method.invoke(instance, args);
            } catch (Exception e) {
                throw new RuntimeException(e.getCause());
            }
        });

        try {
            return future.get(5, TimeUnit.SECONDS); // 5秒超时
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Execution timeout");
        }
    }
}
