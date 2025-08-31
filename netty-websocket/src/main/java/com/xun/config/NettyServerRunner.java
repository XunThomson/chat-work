package com.xun.config;

import com.xun.handler.WebSocketFrameHandler;
import com.xun.listener.NettyShutdownListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: simplify-service
 * @BelongsPackage: com.xun.config
 * @Author: xun
 * @CreateTime: 2025-08-16  22:28
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NettyServerRunner implements CommandLineRunner {

    @Resource
    private WebSocketFrameHandler webSocketFrameHandler;
    @Resource
    private NettyShutdownListener nettyShutdownListener;

    @Value("${server.port}")
    private int serverPort;

    @Value("${netty.websocket.path}")
    private String websocketPath;

    @Value("${netty.websocket.boss-thread-count}")
    private int bossThreadCount;

    @Value("${netty.websocket.worker-thread-count}")
    private int workerThreadCount;

    @Value("${netty.websocket.max-frame-payload-length}")
    private int maxFramePayloadLength;

    @Value("${netty.websocket.idle-time-seconds}")
    private int idleTimeSeconds;

    @Override
    public void run(String... args) {
        // 创建线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount);

        // 注入给 ShutdownListener（用于优雅关闭）
        nettyShutdownListener.setBossGroup(bossGroup);
        nettyShutdownListener.setWorkerGroup(workerGroup);

        // 在新线程中启动 Netty
        new Thread(() -> {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                ch.pipeline()
                                        .addLast(new HttpServerCodec())
                                        .addLast(new HttpObjectAggregator(maxFramePayloadLength))
                                        .addLast(new ChunkedWriteHandler())
                                        .addLast(new IdleStateHandler(0, 0, idleTimeSeconds))
                                        .addLast(webSocketFrameHandler);
                            }
                        })
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = bootstrap.bind(serverPort + 1).sync();
                log.info("NetBar WebSocket 服务已启动，端口: {}", serverPort + 1);

                // 等待服务关闭
                future.channel().closeFuture().sync();

            } catch (Exception e) {
                log.error("NetBar 启动失败", e);
            } finally {
                log.info("NetBar 服务线程退出");
            }
        }, "netty-websocket-server").start();
    }
}


