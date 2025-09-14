package com.xun.orchestrator.module.re;

import com.xun.orchestrator.module.ModuleInstance;
import com.xun.orchestrator.module.mi.LocalModuleInstance;
import com.xun.orchestrator.module.mi.RemoteModuleInstance;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module.re
 * @Author: xun
 * @CreateTime: 2025-09-12  13:35
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
public class ModuleRegistry {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, Bundle> bundles = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> cache = new ConcurrentHashMap<>();
    private final Map<String, ModuleInstance> modules = new ConcurrentHashMap<>();

    public void registerModule(String moduleId, Class<?> instance, Bundle bundle) {
        bundles.put(moduleId, bundle);
        cache.put(moduleId, instance);
    }

    public LocalModuleInstance getLocalModule(String moduleId) {
        return (LocalModuleInstance) modules.computeIfAbsent(moduleId, id -> {
            Bundle bundle = bundles.get(moduleId);
            if (bundle == null) {
                throw new RuntimeException("Bundle not found for module: " + id);
            }
            Class<?> clazz = cache.get(id);
            if (clazz == null) {
                throw new RuntimeException("没有注册过相关内容");
            }
            Object instance = applicationContext.getAutowireCapableBeanFactory()
                    .createBean(clazz);
            if (instance == null) {
                try {
                    bundle.uninstall(); // 此时 bundle 一定不为 null
                } catch (BundleException e) {
                    log.warn("Failed to uninstall bundle for module: {}", id, e);
                }
                throw new IllegalArgumentException("Cannot instantiate @AiModule class: " + clazz.getName());
            }
            return new LocalModuleInstance(moduleId, bundle, instance);
        });
    }

    public RemoteModuleInstance getRemoteModule(String moduleId) {
        return (RemoteModuleInstance) modules.computeIfAbsent(moduleId, id -> {
            Class<?> clazz = cache.get(id);
            if (clazz == null) {
                throw new RuntimeException("没有注册过相关内容");
            }
            Object obj = newInstance(clazz);
            if (!(obj instanceof LocalModuleInstance)) {
                throw new RuntimeException("类型错误");
            }
            return (ModuleInstance) obj;
        });
    }

    public void unloadModule(String moduleId) {
        this.bundles.remove(moduleId);
        this.modules.remove(moduleId);
        // 注意：FunctionRegistry 的清理在 ModuleLoader 中调用
    }

    private <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true); // 支持私有构造器
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法实例化类: " + clazz.getName(), e);
        } catch (Throwable t) {
            throw new RuntimeException("实例化类时发生严重错误: " + clazz.getName(), t);
        }
    }

}
