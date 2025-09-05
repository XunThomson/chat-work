package com.xun.data.mod.impl;

import com.xun.data.mod.DataWriter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.Objects;

public class StrMapWriter implements DataWriter {

    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final Map<String,Object> map;

    public StrMapWriter(StringRedisTemplate stringRedisTemplate,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        Map<String, Object> map) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.map = map;
    }

    @Override
    public void write() {
        if (map == null || map.isEmpty()) {
            throw new IllegalArgumentException("Map cannot be null or empty");
        }
        map.forEach((k, v) -> {
            try {
                Objects.requireNonNull(k, "Map key must not be null");
                stringRedisTemplate.opsForValue().set(k, v == null ? "" : v.toString());

                long timestamp = System.currentTimeMillis();
                String finalKey = k + ":" + timestamp;
                stringRedisTemplate.opsForValue().set(finalKey, String.valueOf(v));

                Map<String, Object> event = Map.of(
                        "event", "data-persisted",
                        "key", finalKey,
                        "timestamp", timestamp,
                        "clientAddress", "192.168.1.100"
                );
                kafkaTemplate.send("data-event",event);
            } catch (Exception e) {

            }
        });
    }
}
