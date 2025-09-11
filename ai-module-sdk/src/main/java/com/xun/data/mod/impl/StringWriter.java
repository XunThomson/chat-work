package com.xun.data.mod.impl;

import com.xun.data.mod.DataWriter;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

            System.out.println(kafkaTemplate);
            System.out.println("-------------------------");

            Map<String, Object> event = Map.of(
                    "event", "data-persisted",
                    "key", finalKey,
                    "timestamp", timestamp,
                    "clientAddress", "192.168.1.100"
            );

            // ✅ 创建真正的 CompletableFuture
            CompletableFuture<SendResult<String, Object>> cf = new CompletableFuture<>();

            // ✅ 使用 kafkaTemplate.send() 返回的 ListenableFuture
            ListenableFuture<SendResult<String, Object>> listenableFuture =
                    (ListenableFuture<SendResult<String, Object>>) kafkaTemplate.send("data-event", event); // 👈 注意：这里应该是 event，不是 val！

            // ✅ 桥接回调
            listenableFuture.addCallback(
                    result -> {
                        System.out.println("✅ 发送成功！");
                        System.out.println("元数据: " + result.getRecordMetadata());
                        cf.complete(result); // 完成 CompletableFuture
                    },
                    ex -> {
                        System.err.println("❌ 发送失败: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                        ex.printStackTrace();
                        cf.completeExceptionally(ex); // 异常完成
                    }
            );

            // ✅ 现在 orTimeout 会真正生效
            cf.orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        if (ex instanceof TimeoutException) {
                            System.err.println("⏰ 发送超时！");
                        }
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("🔥 write 方法异常: ");
            e.printStackTrace();
        }
    }
}
