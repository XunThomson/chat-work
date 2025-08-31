package com.xun.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.entity
 * @Author: xun
 * @CreateTime: 2025-08-18  21:46
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyItem {
    private String type;
    private String description; // 可选字段
}
