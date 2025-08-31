package com.xun.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.entity
 * @Author: xun
 * @CreateTime: 2025-08-19  13:13
 * @Description: TODO
 * @Version: 1.0
 */
@Getter
public class MessageResponse {
    // getter
    private final String type;
    private final String msg;
    private final Map<String, Object> data;

    @JsonCreator
    public MessageResponse(@JsonProperty("type") String type,
                           @JsonProperty("msg") String msg) {
        this.type = type;
        this.msg = msg;
        this.data = new HashMap<>();
    }

    public MessageResponse type(String type) {
        return new MessageResponse(type, this.msg);
    }

    public MessageResponse msg(String msg) {
        return new MessageResponse(this.type, msg);
    }

    public MessageResponse data(String key, Object value) {
        MessageResponse copy = new MessageResponse(this.type, this.msg);
        copy.data.putAll(this.data);
        copy.data.put(key, value);
        return copy;
    }

}
