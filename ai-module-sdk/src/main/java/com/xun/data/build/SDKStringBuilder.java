package com.xun.data.build;

import com.xun.data.mod.DataWriter;
import com.xun.data.mod.impl.StrMapWriter;
import com.xun.data.mod.impl.StringWriter;
import com.xun.data.way.CRBuild;
import com.xun.data.way.WKeyString;
import com.xun.data.way.WMap;
import com.xun.data.way.WValString;
import jakarta.annotation.Resource;
import org.redisson.api.map.MapWriter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class SDKStringBuilder {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate; // 用于发送事件

    public StrBuilder builder() {
        return new StrBuilder(stringRedisTemplate, kafkaTemplate);
    }

    public static class StrBuilder implements WKeyString, WValString, WMap {

        private final StringRedisTemplate stringRedisTemplate;
        private final KafkaTemplate<String, Object> kafkaTemplate;
        private String key;
        private String value;
        private Map<String, Object> map;

        public StrBuilder(StringRedisTemplate stringRedisTemplate, KafkaTemplate<String, Object> kafkaTemplate) {
            this.stringRedisTemplate = stringRedisTemplate;
            this.kafkaTemplate = kafkaTemplate;
        }

        @Override
        public WValString key(String key) {
            this.key = key;
            return this;
        }

        @Override
        public CRBuild value(String value) {
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