package com.xun.orchestrator.module.re;

import com.xun.sdk.annotation.AiFunction;
import com.xun.sdk.annotation.RequiredResources;
import com.xun.sdk.annotation.SharedResource;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class FunctionRegistry {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SharedResourceRegistry sharedResourceRegistry;

    // 复合键: (moduleId + ":" + intentId) -> Class<?>
    private final Map<String, Class<?>> compositeKeyToClassMap = new ConcurrentHashMap<>();
    // 复合键 -> Method (可选优化，避免每次反射查找)
    private final Map<String, Method> compositeKeyToMethodMap = new ConcurrentHashMap<>();
    // Class<?> -> 实例 (OSGi 安全)
    private final Map<Class<?>, Object> instanceCache = new ConcurrentHashMap<>();
    // 复合键 -> RegisteredFunction
    private final Map<String, RegisteredFunction> functionCache = new ConcurrentHashMap<>();

    /**
     * 注册一个候选类（不实例化！）
     *
     * @param clazz    包含 @AiFunction 方法的类
     * @param moduleId 该类所属模块的 ID
     */
    public void registerFunction(Class<?> clazz, String moduleId) {
        RequiredResources resources = clazz.getAnnotation(RequiredResources.class);
        if (resources != null) {
            // 检查 required 资源是否存在
            for (String req : resources.required()) {
                if (!sharedResourceRegistry.hasResource(req)) {
                    throw new IllegalStateException(
                            "Required resource '" + req + "' not found for class: " + clazz.getName()
                    );
                }
            }
            // optional 资源可选，不检查
        }

        for (Method method : clazz.getDeclaredMethods()) {
            AiFunction funcAnno = method.getAnnotation(AiFunction.class);
            if (funcAnno != null && Modifier.isPublic(method.getModifiers())) {
                String intentId = funcAnno.intentId();
                String compositeKey = buildCompositeKey(moduleId, intentId);

                // 检查模块内 intentId 是否重复
                if (compositeKeyToClassMap.containsKey(compositeKey)) {
                    throw new IllegalArgumentException(
                            "Duplicate @AiFunction intentId '" + intentId + "' in module '" + moduleId + "'"
                    );
                }

                compositeKeyToClassMap.put(compositeKey, clazz);
                compositeKeyToMethodMap.put(compositeKey, method); // 预缓存 Method
            }
        }
    }

    public RegisteredFunction getFunction(String intentId, String moduleId, Bundle bundle) {
        String compositeKey = buildCompositeKey(moduleId, intentId);
        return functionCache.computeIfAbsent(compositeKey, key -> {
            Class<?> clazz = compositeKeyToClassMap.get(key);
            if (clazz == null) {
                throw new RuntimeException("未注册函数: " + intentId + " in module: " + moduleId);
            }

            Object instance = instanceCache.computeIfAbsent(clazz, c -> {
                @SuppressWarnings("unchecked")
                Class<Object> cls = (Class<Object>) c; // 安全：Class<?> 可以安全转为 Class<Object>
                return createSpringBeanWithResources(c, bundle);
            });
            if (instance == null) {
                throw new IllegalArgumentException("实例化失败: " + clazz.getName());
            }

            Method method = compositeKeyToMethodMap.get(key);
            AiFunction funcAnno = method.getAnnotation(AiFunction.class);

            return new RegisteredFunction(moduleId, funcAnno, method, instance);
        });
    }

    private <T> T createSpringBeanWithResources(Class<T> clazz, Bundle bundle) {
        try {
            // 使用 Spring 创建 Bean（支持 @Autowired, @Resource）
            T instance = applicationContext.getAutowireCapableBeanFactory()
                    .createBean(clazz);

            // 手动注入 @Resource(name="xxx") 字段（如果 Spring 没自动注入）
            injectDeclaredResources(instance, clazz);

            return instance;
        } catch (Exception e) {
            log.error("Failed to create Spring bean for class: {}", clazz.getName(), e);
            return null;
        }
    }

    private void injectDeclaredResources(Object instance, Class<?> clazz) {

        for (Field field : clazz.getDeclaredFields()) {

            SharedResource anno = field.getAnnotation(SharedResource.class);

            if (null == anno) continue;

            String resourceName = anno.value();
            boolean isOptional = anno.optional();

            sharedResourceRegistry.validateAliasPolicy(resourceName);

            Object resource = sharedResourceRegistry.getResource(resourceName);
            if (resource == null && !isOptional) {
                throw new IllegalStateException(
                        "Required shared resource '" + resourceName + "' not found for field: " +
                                field.getName() + " in class: " + clazz.getName()
                );
            }

            if (resource != null && !field.getType().isInstance(resource)) {
                throw new IllegalStateException(
                        "Resource '" + resourceName + "' type mismatch: expected " +
                                field.getType() + ", but got " + resource.getClass()
                );
            }

            field.setAccessible(true);
            try {
                field.set(instance, resource);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot inject resource into field: " + field, e);
            }


//            Resource resourceAnno = field.getAnnotation(Resource.class);
//            if (resourceAnno != null) {
//                String resourceName = resourceAnno.name();
//                if (resourceName.isEmpty()) {
//                    resourceName = field.getName(); // 默认用字段名
//                }
//
//                Object resource = sharedResourceRegistry.getResource(resourceName);
//                if (resource == null) {
//                    RequiredResources reqAnno = clazz.getAnnotation(RequiredResources.class);
//                    boolean isOptional = reqAnno != null &&
//                            Arrays.asList(reqAnno.optional()).contains(resourceName);
//
//                    if (!isOptional) {
//                        throw new IllegalStateException(
//                                "Required resource '" + resourceName + "' not found for field: " +
//                                        field.getName() + " in class: " + clazz.getName()
//                        );
//                    }
//                    continue; // 可选资源缺失，跳过
//                }
//
//                // 注入资源
//                field.setAccessible(true);
//                try {
//                    field.set(instance, resource);
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException("Cannot inject resource into field: " + field, e);
//                }
//            }
        }
    }


    // 新增：根据 moduleId 卸载所有相关函数
    public void unloadModuleFunctions(String moduleId) {
        // 收集所有属于该模块的复合键
        var keysToRemove = compositeKeyToClassMap.keySet().stream()
                .filter(k -> k.startsWith(moduleId + ":"))
                .toList();

        for (String key : keysToRemove) {
            Class<?> clazz = compositeKeyToClassMap.remove(key);
            compositeKeyToMethodMap.remove(key);
            functionCache.remove(key);

            // 如果该 Class 不再被任何函数引用，可考虑清理实例
            // 注意：一个类可能有多个函数，所以不能简单移除
            // 这里保守处理：不清 instanceCache，避免并发问题
        }
        log.info("Unloaded {} functions for module: {}", keysToRemove.size(), moduleId);
    }

    private String buildCompositeKey(String moduleId, String intentId) {
        return moduleId + ":" + intentId;
    }

    private <T> T newInstance(Class<T> clazz, Bundle bundle) {
        try {
            // 如果 bundle 不为 null，说明是 OSGi 环境，类已由 Bundle ClassLoader 加载
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException e) {
            log.error("无法实例化抽象类或接口: {}", clazz.getName(), e);
            return null;
        } catch (IllegalAccessException e) {
            log.error("构造函数不可访问: {}", clazz.getName(), e);
            return null;
        } catch (NoSuchMethodException e) {
            log.error("找不到无参构造函数: {}", clazz.getName(), e);
            return null;
        } catch (InvocationTargetException e) {
            log.error("构造函数抛出异常: {}", clazz.getName(), e.getTargetException());
            return null;
        } catch (Exception e) {
            log.error("实例化失败（通用异常）: {}", clazz.getName(), e);
            return null;
        } catch (Throwable t) {
            log.error("实例化时发生严重错误: {}", clazz.getName(), t);
            return null;
        }
    }
}