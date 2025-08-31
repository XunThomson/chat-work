package com.xun.listener;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.listener
 * @Author: xun
 * @CreateTime: 2025-08-16  21:57
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
public class NettyShutdownListener implements ApplicationListener<ContextClosedEvent> {

    // 全局 Channel 组，用于统一管理所有活跃连接
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 保存 boss 和 worker 线程组，用于关闭
    private volatile boolean isShuttingDown = false;

    // 通过 setter 注入 Netty 的 EventLoopGroups（可选）
    private io.netty.channel.EventLoopGroup bossGroup;
    private io.netty.channel.EventLoopGroup workerGroup;

    public void setBossGroup(io.netty.channel.EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    public void setWorkerGroup(io.netty.channel.EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    /**
     * 添加 Channel 到全局管理组
     */
    public void addChannel(Channel channel) {
        channelGroup.add(channel);
    }

    /**
     * 移除 Channel
     */
    public void removeChannel(Channel channel) {
        channelGroup.remove(channel);
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return channelGroup.size();
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (isShuttingDown) return;
        isShuttingDown = true;

        log.info("NetBar 正在执行优雅关闭，当前活跃连接数: {}", channelGroup.size());

        try {
            // 通知客户端（可选）
            channelGroup.writeAndFlush("SERVER_CLOSING");
            channelGroup.close().sync(); // 等待关闭

            // 关闭线程组
            if (bossGroup != null) {
                bossGroup.shutdownGracefully(5, 15, TimeUnit.SECONDS);
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully(5, 15, TimeUnit.SECONDS);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("优雅关闭被中断", e);
        } catch (Exception e) {
            log.error("优雅关闭失败", e);
        } finally {
            log.info("NetBar 服务已安全关闭");
        }
    }

    // 可暴露为 Actuator Endpoint 用于监控
    public boolean isShuttingDown() {
        return isShuttingDown;
    }
}
