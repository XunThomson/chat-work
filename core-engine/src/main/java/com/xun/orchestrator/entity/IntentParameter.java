package com.xun.orchestrator.entity;

import java.util.List;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.entity
 * @Author: xun
 * @CreateTime: 2025-08-23  21:55
 * @Description: TODO 意图参数
 * @Version: 1.0
 */
public class IntentParameter {
    private String name;              // 参数名：days, reason
    private String displayName;       // 显示名：天数、请假原因
    private String type;              // 类型：string, number, date, enum
    private boolean required;         // 是否必填
    private Integer minLength;
    private Integer maxLength;
    private Double minNumber;
    private Double maxNumber;
    private List<String> allowedValues; // 枚举值
    private String regexPattern;        // 正则校验
    private String prompt;              // 主动追问话术："您要请几天假？"
    private String example;             // 示例："3"
    private boolean sensitive;          // 是否敏感信息（需脱敏）
}
