package com.xun.consumer;

import com.xun.entity.StrInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.consumer
 * @Author: xun
 * @CreateTime: 2025-09-07  13:39
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Component
public class InfoConsumer {
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @KafkaListener(
            topics = "data-event",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
//            Map<String, Object> value = record.value();
            System.out.printf("📨 收到消息: topic=%s, partition=%d, offset=%d, value=%s%n",
                    record.topic(), record.partition(), record.offset(), record.value());

            // 手动提交偏移量
            ack.acknowledge();

        } catch (Exception e) {
            log.error("处理消息失败", e);
            ack.acknowledge();
        }
    }

    private void processOrder(StrInfo info) {
        // 业务逻辑
    }
}
