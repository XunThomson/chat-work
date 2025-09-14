package com.xun.data.build;

import com.xun.data.mod.DataWriter;
import com.xun.data.mod.impl.StrMapWriter;
import com.xun.data.mod.impl.StringWriter;
import com.xun.data.way.CRBuild;
import com.xun.data.way.WKeyString;
import com.xun.data.way.WMap;
import com.xun.data.way.WValString;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

public class SDKStringBuilder {

    private final StringRedisTemplate stringRedisTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate; // 用于发送事件

    private SDKStringBuilder(StringRedisTemplate stringRedisTemplate,
                             KafkaTemplate<String, Object> kafkaTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public static SDKStringBuilder create(StringRedisTemplate redisTemplate,
                                          KafkaTemplate<String, Object> kafkaTemplate) {
        return new SDKStringBuilder(redisTemplate, kafkaTemplate);
    }


    public StrBuilder builder() {
        System.out.println("✅ builder() 方法被调用！");

        StrBuilder instance = new StrBuilder(stringRedisTemplate, kafkaTemplate);
        System.out.println("🏗️ 实例创建完成: " + instance);
        System.out.println("📤 准备返回实例...");

        // 👇 加这一行！如果这行能打印，说明类加载成功！
        System.out.println("🧪 接口类存在: " + WKeyString.class);

        return instance; // 👈 如果这行之后没日志，说明 return 时类加载失败！
    }

    public static class StrBuilder implements WKeyString, WValString, WMap {

        static {
            System.out.println("🧪 StrBuilder 静态初始化开始...");
            // 如果这里有依赖外部类的代码，可能在这里崩溃！
            System.out.println("🧪 StrBuilder 静态初始化完成！");
        }


        private final StringRedisTemplate stringRedisTemplate;
        private final KafkaTemplate<String, Object> kafkaTemplate;
        private String key;
        private String value;
        private Map<String, Object> map;

        public StrBuilder(StringRedisTemplate stringRedisTemplate, KafkaTemplate<String, Object> kafkaTemplate) {
            System.out.println("🏗️ StrBuilder 构造函数开始...");
            this.stringRedisTemplate = stringRedisTemplate;
            this.kafkaTemplate = kafkaTemplate;
            System.out.println("✅ StrBuilder 构造完成！");
        }

        @Override
        public WValString key(String key) {
            System.out.println(key);
            this.key = key;
            return this;
        }

        @Override
        public CRBuild value(String value) {
            System.out.println(value);
            StringWriter stringWriter = new StringWriter(stringRedisTemplate, kafkaTemplate, this.key, value);
            return new BuildResult(stringWriter);
        }

        @Override
        public CRBuild map(Map<String, Object> map) {
            DataWriter writer = new StrMapWriter(stringRedisTemplate,kafkaTemplate, map);
            return new BuildResult(writer);
        }
    }

    public static class BuildResult implements CRBuild {

        private final DataWriter writer;

        public BuildResult(DataWriter writer) {
            this.writer = writer;
        }

        @Override
        public CRBuild build() {
            writer.write();
            return this;
        }

    }
}