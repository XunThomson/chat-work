package com.xun.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.utils
 * @Author: xun
 * @CreateTime: 2025-08-21  21:43
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class AiVisitDaoTools {


    @Resource
    private ObjectMapper objectMapper;


//    @Tool(description = "获取模块列表（分页结果）")
//    String getModules() {
//        try {
//            IPage<Map<String, Object>> modulePages = serviceModuleService.getAllModulePage();
//            List<Map<String, Object>> modules = modulePages.getRecords().stream()
//                    .filter(m -> ((Byte) m.get("is_active")).equals((byte) 1))
//                    .map(m -> Map.of(
//                            "name", m.get("name"),
//                            "description", m.get("description"),
//                            "category", m.get("category")
//                    )) // 只保留模型需要的字段
//                    .collect(Collectors.toList());
//            return objectMapper.writeValueAsString(modules);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @Tool(description = "根据用户需求获取与模块名相同的工具。")
//    String getGetModuleFunctions(String moduleName,Byte accountEntitlementLevel) {
//
//        try {
//            IPage<Map<String, Object>> modulePages = serviceModuleService.getModuleByName(moduleName, accountEntitlementLevel);
//
//            List<Map<String, Object>> modules = modulePages.getRecords().stream()
//                    .filter(m -> ((Byte) m.get("is_active")).equals((byte) 1))
//                    .map(m -> Map.of(
//                            "name", m.get("name"),
//                            "description", m.get("description"),
//                            "category", m.get("category")
//                    )) // 只保留模型需要的字段
//                    .collect(Collectors.toList());
//            return objectMapper.writeValueAsString(modules);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
