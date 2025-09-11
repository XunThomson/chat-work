package com.xun.orchestrator.config;

import com.xun.data.build.SDKStringBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.config
 * @Author: xun
 * @CreateTime: 2025-09-04  23:47
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
@ConditionalOnClass({StringRedisTemplate.class, KafkaTemplate.class})
@EnableConfigurationProperties(DataBuildProperties.class)
public class DataBuildAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(DataBuildProperties properties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(properties.getRedisHost());
        config.setPort(properties.getRedisPort());
        if (properties.getRedisPassword() != null) {
            config.setPassword(RedisPassword.of(properties.getRedisPassword()));
        }
        config.setDatabase(properties.getRedisDatabase());

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet(); // 初始化

        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(KafkaTemplate.class)
    public KafkaTemplate<String, Object> kafkaTemplate(DataBuildProperties properties) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafkaBootstrap());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        configs.put("security.protocol", "SASL_PLAINTEXT");
        configs.put("sasl.mechanism", "PLAIN");
        configs.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"admin\" " +
                        "password=\"123456\";");

        // ✅ 关键：超时和重试配置
        configs.put(ProducerConfig.ACKS_CONFIG, "1"); // 或 "all"
        configs.put(ProducerConfig.RETRIES_CONFIG, 3);// 重试
        configs.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false); // 如果不启用幂等性

        // ✅ 元数据刷新
        configs.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 30000); // 30秒刷新一次

        ProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(configs);
        return new KafkaTemplate<>(pf);
    }


    @Bean
    public SDKStringBuilder sdkStringBuilder(
            StringRedisTemplate stringRedisTemplate,
            KafkaTemplate<String, Object> kafkaTemplate) {
        return SDKStringBuilder.create(stringRedisTemplate, kafkaTemplate);
    }
}
