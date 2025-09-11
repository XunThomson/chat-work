package com.xun.orchestrator.module;

import com.xun.orchestrator.module.mi.LocalModuleInstance;
import com.xun.orchestrator.module.mi.RemoteModuleInstance;
import com.xun.sdk.annotation.AiFunction;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module
 * @Author: xun
 * @CreateTime: 2025-08-23  21:11
 * @Description: TODO 全局模块与功能注册中心
 * @Version: 1.0
 */
@Component
public class ModuleRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final Map<String, ModuleInstance> modules = new ConcurrentHashMap<>();
    private final Map<String, RegisteredFunction> functions = new ConcurrentHashMap<>();
    public void registerModule(LocalModuleInstance instance) {
        modules.put(instance.getModuleId(), instance);
    }

    public void registerModule(RemoteModuleInstance instance) {
        modules.put(instance.getModuleId(), instance);
    }


    public void registerFunction(String intentId, RegisteredFunction func) {
        functions.put(intentId, func);
    }

    public LocalModuleInstance getLocalModule(String moduleId) {
        return (LocalModuleInstance) modules.get(moduleId);
    }

    public RemoteModuleInstance getRemoteModule(String moduleId) {
        return (RemoteModuleInstance) modules.get(moduleId);
    }

    public RegisteredFunction getFunction(String intentId) {
        return functions.get(intentId);
    }

    public Map<String, RegisteredFunction> getAllFunctions() {
        return functions;
    }

    public void unloadModule(String moduleId){
        this.modules.remove(moduleId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    // 内部类：注册的功能元信息
    @Getter
    public static class RegisteredFunction {
        // Getters
        private final String moduleId;
        private final String intentId;
        private final AiFunction annotation;
        private final java.lang.reflect.Method method;
        private final Object instance;

        public RegisteredFunction(String moduleId, AiFunction annotation,
                                  java.lang.reflect.Method method, Object instance) {
            this.moduleId = moduleId;
            this.annotation = annotation;
            this.method = method;
            this.instance = instance;
            this.intentId = annotation.intentId();
        }

    }
}
