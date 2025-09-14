package com.xun.orchestrator.module.re;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module.re
 * @Author: xun
 * @CreateTime: 2025-09-12  21:46
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
public class SharedResourceRegistry {

    // 主资源池：canonicalName → ResourceMetadata
    private final Map<String, ResourceMetadata> canonicalResources = new ConcurrentHashMap<>();

    // 别名映射：alias → canonicalName
    private final Map<String, String> aliasToCanonical = new ConcurrentHashMap<>();

    /**
     * 注册主资源（官方名称）
     */
    public void registerCanonicalResource(String canonicalName, Object resource, boolean allowAlias) {
        if (canonicalName == null || canonicalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Canonical name cannot be null or empty");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        ResourceMetadata meta = new ResourceMetadata(canonicalName, resource, allowAlias);
        ResourceMetadata existing = canonicalResources.putIfAbsent(canonicalName, meta);
        if (existing != null) {
            throw new IllegalArgumentException("Canonical resource already registered: " + canonicalName);
        }

        // 自动将官方名也加入别名映射（自己是自己的别名）
        aliasToCanonical.put(canonicalName, canonicalName);
    }

    /**
     * 注册别名（必须指向一个已存在的 canonicalName）
     */
    public void registerAlias(String alias, String canonicalName) {
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalArgumentException("Alias cannot be null or empty");
        }
        if (canonicalName == null || canonicalName.trim().isEmpty()) {
            throw new IllegalArgumentException("CanonicalName cannot be null or empty");
        }

        ResourceMetadata meta = canonicalResources.get(canonicalName);
        if (meta == null) {
            throw new IllegalArgumentException("Canonical resource not found: " + canonicalName);
        }
        if (!meta.isAllowAlias()) {
            throw new IllegalArgumentException(
                    "Resource '" + canonicalName + "' does not allow aliases. Cannot register alias: " + alias
            );
        }

        String existing = aliasToCanonical.putIfAbsent(alias, canonicalName);
        if (existing != null) {
            throw new IllegalArgumentException("Alias already registered: " + alias);
        }
    }

    /**
     * 根据名称（可能是别名或官方名）获取资源，自动解析并校验策略
     */
    public Object getResource(String name) {
        String canonicalName = aliasToCanonical.get(name);
        if (canonicalName == null) {
            return null; // 不存在
        }

        ResourceMetadata meta = canonicalResources.get(canonicalName);
        if (meta == null) {
            log.warn("Inconsistent state: alias {} points to non-existent canonical resource {}", name, canonicalName);
            return null;
        }

        return meta.getResource();
    }

    /**
     * 校验：如果该名称指向的资源不允许别名，那么 name 必须等于 canonicalName
     * @throws IllegalStateException 如果违反别名策略
     */
    public void validateAliasPolicy(String requestedName) {
        String canonicalName = aliasToCanonical.get(requestedName);
        if (canonicalName == null) return; // 不存在，不校验

        if (!requestedName.equals(canonicalName)) {
            // 使用了别名 → 检查该资源是否允许别名
            ResourceMetadata meta = canonicalResources.get(canonicalName);
            if (meta != null && !meta.isAllowAlias()) {
                throw new IllegalStateException(
                        "Resource '" + canonicalName + "' does not allow aliases. " +
                                "You must use the canonical name, not alias: " + requestedName
                );
            }
        }
        // 如果 requestedName == canonicalName，总是允许
    }

    public boolean hasResource(String name) {
        return aliasToCanonical.containsKey(name);
    }

    public Set<String> getAllCanonicalNames() {
        return canonicalResources.keySet();
    }
}

