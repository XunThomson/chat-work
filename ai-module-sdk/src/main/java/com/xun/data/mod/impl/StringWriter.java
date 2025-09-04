package com.xun.data.mod.impl;

import com.xun.data.mod.DataWriter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

public class StringWriter implements DataWriter {

    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String key;
    private final String val;

    public StringWriter(StringRedisTemplate stringRedisTemplate,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        String key,
                        String val) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.key = key;
        this.val = val;
    }

    @Override
    public void write() {
        try {
            long timestamp = System.currentTimeMillis();
            String finalKey = key + ":" + timestamp;
            stringRedisTemplate.opsForValue().set(finalKey, val);

            Map<String, Object> event = Map.of(
                    "event", "data-persisted",
                    "key", finalKey,
                    "timestamp", timestamp,
                    "clientAddress", "192.168.1.100"
            );
            kafkaTemplate.send("data-event",event);
        } catch (Exception e) {
            System.err.println("Kafka send failed: " + e.getMessage());
        }
    }
}
