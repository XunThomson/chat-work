//package com.xun.orchestrator.module.re.facade;
//
//import com.xun.orchestrator.module.mi.LocalModuleInstance;
//import com.xun.orchestrator.module.re.FunctionRegistry;
//import com.xun.orchestrator.module.re.ModuleRegistry;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//
///**
// * @BelongsProject: ai-orchestrator-pro
// * @BelongsPackage: com.xun.orchestrator.module.re.facade
// * @Author: xun
// * @CreateTime: 2025-09-12  21:47
// * @Description: TODO
// * @Version: 1.0
// */
//@Component
//public class ModuleOrchestrator implements ApplicationContextAware {
//
//    @Autowired
//    private ModuleRegistry moduleRegistry;
//    @Autowired
//    private FunctionRegistry functionRegistry;
//    @Autowired
//    private ResourceDependencyRegistry dependencyRegistry;
//    @Autowired
//    private ResourceRegistry resourceRegistry; // 你之前定义的资源注册中心
//
//    private ApplicationContext applicationContext;
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
//    // ✅ 注册本地模块（含函数 + 依赖）
//    public void registerLocalModule(LocalModuleInstance instance) {
//        try {
//            moduleRegistry.register(instance);
//            registerFunctions(instance);
//            registerDependencies(instance);
//            System.out.println("✅ 模块注册成功: " + instance.getModuleId());
//        } catch (Exception e) {
//            // 回滚
//            moduleRegistry.unload(instance.getModuleId());
//            functionRegistry.unregisterByModule(instance.getModuleId());
////            dependencyRegistry.unload(instance.getModuleId());
//            throw new RuntimeException("模块注册失败: " + instance.getModuleId(), e);
//        }
//    }
//
//    // ✅ 注册远程模块
//    public void registerRemoteModule(RemoteModuleInstance instance) {
//        moduleRegistry.register(instance);
//        // 远程模块函数和依赖由远程服务提供，本地只注册元信息
//    }
//
//    // ✅ 卸载模块（事务性）
//    public void unloadModule(String moduleId) {
//        functionRegistry.unregisterByModule(moduleId);
//        dependencyRegistry.unload(moduleId);
//        moduleRegistry.unload(moduleId);
//        System.out.println("🗑️ 模块卸载: " + moduleId);
//    }
//
//    // ✅ 启动时全局校验
//    public void validateAllDependencies() {
//        dependencyRegistry.validateAll(resourceRegistry);
//    }
//
//    // ✅ 获取函数（门面方法）
//    public RegisteredFunction getFunction(String intentId) {
//        return functionRegistry.get(intentId);
//    }
//
//    // ✅ 获取模块
//    public ModuleInstance getModule(String moduleId) {
//        return moduleRegistry.get(moduleId);
//    }
//
//    // --- 私有方法 ---
//
//    private void registerFunctions(LocalModuleInstance instance) {
//        Class<?> clazz = instance.getInstance().getClass();
//        for (Method method : clazz.getDeclaredMethods()) {
//            AiFunction ann = method.getAnnotation(AiFunction.class);
//            if (ann != null) {
//                RegisteredFunction func = new RegisteredFunction(
//                        instance.getModuleId(), ann, method, instance.getInstance()
//                );
//                functionRegistry.register(func);
//            }
//        }
//    }
//
//    private void registerDependencies(LocalModuleInstance instance) {
//        Class<?> clazz = instance.getInstance().getClass();
//        RequiredResources ann = clazz.getAnnotation(RequiredResources.class);
//        if (ann != null) {
//            dependencyRegistry.register(
//                    instance.getModuleId(),
//                    ann.required(),
//                    ann.optional()
//            );
//        }
//    }
//}
