package com.xun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.config
 * @Author: xun
 * @CreateTime: 2025-09-07  13:40
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
public class KafkaConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}