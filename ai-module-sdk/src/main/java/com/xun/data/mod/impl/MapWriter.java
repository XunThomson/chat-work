package com.xun.data.mod.impl;

import com.xun.data.mod.DataWriter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;


import java.util.Map;

//@Component
public class MapWriter implements DataWriter {
    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String key;
    private final Map<String,Object> objMap;

    public MapWriter(StringRedisTemplate stringRedisTemplate,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        String key,
                        Map<String,Object> map) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.key = key;
        this.objMap = map;
    }

    @Override
    public void write() {
        try {
            long timestamp = System.currentTimeMillis();
            String finalKey = key + ":" + timestamp;
            stringRedisTemplate.opsForHash().putAll(finalKey,objMap);

            Map<String, Object> event = Map.of(
                    "event", "data-persisted",
                    "key", finalKey,
                    "timestamp", timestamp,
                    "clientAddress", "192.168.1.100"
            );
            kafkaTemplate.send("data-event",event);
        } catch (Exception e) {

        }
    }
}
