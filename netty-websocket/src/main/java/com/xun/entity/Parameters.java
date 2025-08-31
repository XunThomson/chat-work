package com.xun.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.entity
 * @Author: xun
 * @CreateTime: 2025-08-18  21:45
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameters {
    private String type;
    private Properties properties;
    private List<String> required;
}
