//package com.xun.orchestrator.module.re;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.kafka.core.KafkaResourceFactory;
//import org.springframework.stereotype.Component;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @BelongsProject: ai-orchestrator-pro
// * @BelongsPackage: com.xun.orchestrator.module.re
// * @Author: xun
// * @CreateTime: 2025-09-13  16:32
// * @Description: TODO
// * @Version: 1.0
// */
//@Component
//public class SmartPluginContainer implements ApplicationContextAware {
//
//    private ApplicationContext applicationContext;
//    private final Map<String, Class<?>> pluginClassCache = new ConcurrentHashMap<>();
//    private final Map<String, Object> pluginInstanceCache = new ConcurrentHashMap<>();
//
//    // 可配置：插件空闲超时时间（单位：秒）
//    @Value("${plugin.idle.timeout:300}")
//    private int idleTimeoutSeconds;
//
//    // 资源工厂（按需创建）
//    @Autowired
//    private KafkaResourceFactory kafkaFactory;
//    @Autowired
//    private RedisResourceFactory redisFactory;
//
//    // 依赖注入器
//    @Autowired private AutowireCapableBeanFactory beanFactory;
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }
//
//    /**
//     * 懒加载 + 按需注入 + 资源绑定
//     */
//    public Object getPluginInstance(String pluginClassName) throws Exception {
//        // 1. 缓存 Class（轻量，不占资源）
//        Class<?> clazz = pluginClassCache.computeIfAbsent(pluginClassName, cn -> {
//            try {
//                return Class.forName(cn);
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException("插件类未找到: " + cn, e);
//            }
//        });
//
//        // 2. 缓存实例（懒创建）
//        return pluginInstanceCache.computeIfAbsent(pluginClassName, key -> {
//            try {
//                Object instance = clazz.getDeclaredConstructor().newInstance();
//
//                // 3. ✅ 按需注入依赖（根据 @RequiredResources）
//                injectRequiredResources(instance, clazz);
//
//                // 4. ✅ 注入 Spring 管理的 Bean（如 @Resource SDKStringBuilder）
//                beanFactory.autowireBean(instance);
//
//                System.out.println("✅ 插件实例已创建并注入依赖: " + pluginClassName);
//                return instance;
//            } catch (Exception e) {
//                throw new RuntimeException("插件实例化失败: " + pluginClassName, e);
//            }
//        });
//    }
//
//    private void injectRequiredResources(Object instance, Class<?> clazz) {
//        RequiredResources ann = clazz.getAnnotation(RequiredResources.class);
//        if (ann == null) return;
//
//        // 反射获取字段并注入（你也可以用 setter）
//        try {
//            if (ann.kafka()) {
//                Field kafkaField = findField(clazz, KafkaTemplate.class);
//                if (kafkaField != null) {
//                    kafkaField.setAccessible(true);
//                    kafkaField.set(instance, kafkaFactory.getSharedKafkaTemplate());
//                }
//            }
//            if (ann.redis()) {
//                Field redisField = findField(clazz, StringRedisTemplate.class);
//                if (redisField != null) {
//                    redisField.setAccessible(true);
//                    redisField.set(instance, redisFactory.getSharedRedisTemplate());
//                }
//            }
//            // ... 其他资源
//        } catch (Exception e) {
//            throw new RuntimeException("资源注入失败", e);
//        }
//    }
//
//    private Field findField(Class<?> clazz, Class<?> fieldType) {
//        for (Field field : clazz.getDeclaredFields()) {
//            if (fieldType.isAssignableFrom(field.getType())) {
//                return field;
//            }
//        }
//        return null;
//    }
//}
