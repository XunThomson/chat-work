package com.xun.orchestrator.security;

import com.xun.sdk.model.AiFunctionResult;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
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
@Slf4j
public class SandboxExecutor {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ExecutorService executorService;

    public SandboxExecutor() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(2, availableProcessors);

        this.executorService = new ThreadPoolExecutor(
                poolSize,                    // corePoolSize
                poolSize,                    // maxPoolSize
                60L,                         // keepAliveTime
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200), // 有界队列
                r -> {
                    Thread t = new Thread(r, "sandbox-worker-" + System.nanoTime());
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler((thread, throwable) ->
                            log.error("Uncaught exception in sandbox thread [{}]", thread.getName(), throwable)
                    );
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝时由调用者执行，防止崩溃
        );
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("SandboxExecutor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @description: 在沙箱中执行方法
     * @author: xun 
     * @date: 2025/8/31 16:01
     * @param: [instance, method, args]
     * @return: com.xun.sdk.model.AiFunctionResult
     **/
    public AiFunctionResult executeInSandbox(Object instance, Method method, Object[] args) {
        Future<Object> future = executorService.submit(() -> {
            try {
                if (instance == null) {
                    throw new IllegalArgumentException("Instance is null");
                }
                if (method == null) {
                    throw new IllegalArgumentException("Method is null");
                }
                return method.invoke(instance, args);
            } catch (InvocationTargetException e) {
                // 正确处理反射调用异常
                throw new RuntimeException("Method execution failed", e.getCause());
            } catch (Exception e) {
                // 保留异常信息
                throw new RuntimeException("Reflection invoke error", e);
            }
        });

        try {
            Object result = future.get(5, TimeUnit.SECONDS);
            return AiFunctionResult.success(result.toString());
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("Sandbox execution timeout: method={}, instance={}", method, instance);
            return AiFunctionResult.failure("TIMEOUT", "Execution timeout (5s)");
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            return AiFunctionResult.failure("INTERRUPTED", "Execution was interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                return AiFunctionResult.failure("RUNTIME_ERROR", runtimeException.getMessage());
            }
            return AiFunctionResult.failure("EXECUTION_ERROR", cause.getMessage());
        } catch (Exception e) {
            return AiFunctionResult.failure("INTERNAL_ERROR", "Unexpected error: " + e.getMessage());
        }
    }
}
