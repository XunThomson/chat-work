package com.xun.utils;

import com.xun.constant.RedisKeys;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.utils
 * @Author: xun
 * @CreateTime: 2025-08-16  21:53
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
public class ChannelUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 本地缓存：channelId -> Channel（只缓存本节点的连接）
    private static final Map<String, Channel> localChannelCache = new ConcurrentHashMap<>();

    // ==================== 绑定与解绑 ====================

    /**
     * 绑定用户（存入 Redis + 本地缓存）
     */
    public void bindUser(Channel channel, String userId) {
        String channelId = channel.id().asShortText();
        String nodeId = getNodeId(); // 如："node-1"

        // 1. 本地缓存
        localChannelCache.put(channelId, channel);

        // 2. Redis 存储映射关系
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set(RedisKeys.CHANNEL_USER + channelId, userId, Duration.ofHours(2));
        ops.set(RedisKeys.USER_CHANNEL + userId, channelId, Duration.ofHours(2));
        stringRedisTemplate.opsForSet().add(RedisKeys.ONLINE_SET, userId);

        // 可选：记录节点信息（用于路由）
        // ops.set(RedisKeys.CHANNEL_NODE + channelId, nodeId, Duration.ofHours(2));

        log.info("用户绑定成功: userId={}, channelId={}, node={}", userId, channelId, nodeId);
    }

    /**
     * 解绑用户（清理 Redis 和本地）
     */
    public void unbindUser(Channel channel) {
        String channelId = channel.id().asShortText();
        String userId = getUserId(channel);

        // 1. 清理本地
        localChannelCache.remove(channelId);

        if (userId != null) {
            // 2. 清理 Redis
            stringRedisTemplate.delete(RedisKeys.CHANNEL_USER + channelId);
            stringRedisTemplate.delete(RedisKeys.USER_CHANNEL + userId);
            stringRedisTemplate.opsForSet().remove(RedisKeys.ONLINE_SET, userId);

            log.info("用户解绑: userId={}, channelId", userId, channelId);
        }
    }

    // ==================== 查询方法 ====================

    /**
     * 获取当前 Channel 对应的用户 ID
     */
    public String getUserId(Channel channel) {
        String channelId = channel.id().asShortText();
        return stringRedisTemplate.opsForValue().get(RedisKeys.CHANNEL_USER + channelId);
    }

    /**
     * 判断用户是否在线（任意节点）
     */
    public boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(RedisKeys.USER_CHANNEL + userId)
        );
    }

    /**
     * 获取在线用户数
     */
    public Long getOnlineCount() {
        return stringRedisTemplate.opsForSet().size(RedisKeys.ONLINE_SET);
    }

    /**
     * 获取所有在线用户 ID 列表
     */
    public Set<String> getOnlineUserIds() {
        return stringRedisTemplate.opsForSet().members(RedisKeys.ONLINE_SET);
    }

    // ==================== 本地 Channel 操作 ====================

    /**
     * 获取本节点上的 Channel（仅限本机连接）
     */
    public Channel getLocalChannel(String channelId) {
        return localChannelCache.get(channelId);
    }

    public Collection<Channel> getAllLocalChannels() {
        return localChannelCache.values();
    }

    /**
     * 关闭并清理连接
     */
    public void closeChannel(Channel channel) {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
        unbindUser(channel);
    }

    // ==================== 工具方法 ====================

    private String getNodeId() {
        // 可从配置或环境变量读取，如 node-1, node-2
        return "node-" + Runtime.getRuntime().availableProcessors();
    }
}
