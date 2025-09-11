package com.xun.orchestrator.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.config
 * @Author: xun
 * @CreateTime: 2025-09-04  23:52
 * @Description: TODO
 * @Version: 1.0
 */
@ConfigurationProperties("xun.data.build")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataBuildProperties {

    private String redisHost = "192.168.92.109";
    private int redisPort = 6379;
    private String redisPassword = "123456";
    private int redisDatabase = 0;

    private String kafkaBootstrap = "192.168.92.109:19094";
    private String kafkaTopic = "data-event";

}
