package com.xun.orchestrator.module.re;

import com.xun.data.build.SDKStringBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module.re
 * @Author: xun
 * @CreateTime: 2025-09-13  20:48
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class SharedResourceInitializer {

    @Autowired
    private SharedResourceRegistry registry;

    @Autowired
    private ApplicationContext applicationContext;

    public SharedResourceInitializer() {
        System.out.println("🚀 SharedResourceInitializer 构造函数被调用 —— 说明 Spring 创建了我！");
    }

    @PostConstruct
    public void init() {
        SDKStringBuilder sdkStringBuilder = applicationContext.getBean(SDKStringBuilder.class);

        if (sdkStringBuilder == null) {
            throw new RuntimeException("SDKStringBuilder is NULL! Registration failed.");
        }

        System.err.println("Registering SDKStringBuilder: " + sdkStringBuilder);

        registry.registerCanonicalResource("sdk-str-builder", sdkStringBuilder, true);
        registry.registerAlias("str-builder", "sdk-str-builder");

        System.err.println("✅ Resource 'str-builder' registered with instance: " + sdkStringBuilder);



//        eg:
//        SaveService save = new SaveService();
//        RedisService redis = new RedisService();

        // ✅ 注册官方资源：save（允许别名）
//        registry.registerCanonicalResource("save", save, true);

        // ✅ 注册官方资源：securityAudit（禁止别名）
//        SecurityAuditService audit = new SecurityAuditService();
//        registry.registerCanonicalResource("securityAudit", audit, false);

        // ✅ 注册别名
//        registry.registerAlias("userSave", "save");
//        registry.registerAlias("orderSave", "save");
//        registry.registerAlias("cache", "redis");

        // ❌ 下面这行会抛异常：securityAudit 不允许别名
        // registry.registerAlias("audit", "securityAudit"); ← 非法！
    }
}
