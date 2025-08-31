package com.xun.orchestrator.entity;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.entity
 * @Author: xun
 * @CreateTime: 2025-08-23  21:54
 * @Description: TODO
 * @Version: 1.0
 */
public class AiIntent {

    // ========== 基础信息 ==========
    private String intentId;            // 全局唯一ID: com.ai.hr.leave.apply
    private String moduleId;            // 所属模块ID
    private String functionName;        // 功能名（对外显示）
    private String description;         // 功能描述

    // ========== 语义理解 ==========
    private List<String> userUtterances; // 用户可能说的话（训练语料）
    private List<String> synonyms;       // 同义词扩展：请假/请个假/我要休假
    private String domain;               // 领域：HR、Finance、IT
    private String category;             // 分类：审批、查询、操作

    // ========== 参数定义（带约束） ==========
    private List<IntentParameter> parameters;

    // ========== 对话行为 ==========
    private boolean requiresContext;     // 是否依赖上下文
    private Integer maxConversationTurns; // 最大多轮对话轮数
    private String confirmationPolicy;   // 确认策略：ALWAYS, AUTO, NEVER
    private String fallbackResponse;     // 无法处理时的回复

    // ========== 权限与安全 ==========
    private List<String> requiredRoles;  // 所需角色
    private List<String> requiredScopes; // 所需权限范围
    private boolean auditRequired;       // 是否需要审计日志

    // ========== 生命周期 ==========
    private String version;
    private boolean enabled;             // 是否启用
    private String deprecationNotice;    // 弃用提示

    // ========== 扩展字段 ==========
    private Map<String, Object> metadata; // JSON 扩展（如：SLA、优先级、冷启动权重）

}
