package com.xun.constant;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.constant
 * @Author: xun
 * @CreateTime: 2025-08-17  18:03
 * @Description: TODO
 * @Version: 1.0
 */
public class RedisKeys {
    public static final String CHANNEL_USER = "ws:channel:user:";     // channel_id -> userId
    public static final String USER_CHANNEL = "ws:user:channel:";     // userId -> channel_id
    public static final String ONLINE_SET   = "ws:online:users";      // Set<String> 在线用户集合
    public static final String CHANNEL_NODE = "ws:channel:node:";     // channel_id -> node_id (可选)

    public static final String TOPIC_PRIVATE_MSG = "ws:private:message"; // 私信频道
    public static final String TOPIC_PRIVATE_PLUS_MSG = "ws:privateplus:message"; // 私信频道
    public static final String TOPIC_BROADCAST   = "ws:broadcast:all";   // 广播频道
}
