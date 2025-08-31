//package com.xun.config;
//
//import com.xun.handler.WebSocketFrameHandler;
//import com.xun.listener.NettyShutdownListener;
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.codec.http.HttpObjectAggregator;
//import io.netty.handler.codec.http.HttpServerCodec;
//import io.netty.handler.stream.ChunkedWriteHandler;
//import io.netty.handler.timeout.IdleStateHandler;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.event.ContextClosedEvent;
//import org.springframework.stereotype.Component;
//
///**
// * @BelongsProject: simplify-service
// * @BelongsPackage: com.xun.config
// * @Author: xun
// * @CreateTime: 2025-08-16  21:46
// * @Description: TODO
// * @Version: 1.0
// */
//@Component
//@Slf4j
//public class NettyConfig {
//
//    @Value("${server.port}")
//    private int serverPort;
//
//    @Value("${netty.websocket.path}")
//    private String websocketPath;
//
//    @Value("${netty.websocket.boss-thread-count}")
//    private int bossThreadCount;
//
//    @Value("${netty.websocket.worker-thread-count}")
//    private int workerThreadCount;
//
//    @Value("${netty.websocket.max-frame-payload-length}")
//    private int maxFramePayloadLength;
//
//    @Value("${netty.websocket.idle-time-seconds}")
//    private int idleTimeSeconds;
//
//    @Resource
//    private WebSocketFrameHandler webSocketFrameHandler;
//
//    @Resource
//    private NettyShutdownListener nettyShutdownListener;
//
//    @PostConstruct
//    public void start() throws InterruptedException {
//        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
//        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount);
//
//        try {
//            ServerBootstrap bootstrap = new ServerBootstrap();
//            bootstrap.group(bossGroup, workerGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) {
//                            ChannelPipeline pipeline = ch.pipeline();
//                            pipeline.addLast(new HttpServerCodec());
//                            pipeline.addLast(new HttpObjectAggregator(65536));
//                            pipeline.addLast(new ChunkedWriteHandler());
//                            pipeline.addLast(new IdleStateHandler(0, 0, idleTimeSeconds));
//                            pipeline.addLast(webSocketFrameHandler);
//                        }
//                    })
//                    .childOption(ChannelOption.SO_KEEPALIVE, true);
//
//            ChannelFuture future = bootstrap.bind(serverPort + 1).sync(); // 使用 8081 端口
//            log.info("NetBar WebSocket 服务已启动，端口: {}", serverPort + 1);
//
//            // 添加 JVM 关闭钩子
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//
//                nettyShutdownListener.onApplicationEvent(new ContextClosedEvent(this));
//
//                log.info("NetBar 正在优雅关闭...");
//                bossGroup.shutdownGracefully();
//                workerGroup.shutdownGracefully();
//            }));
//
//            future.channel().closeFuture().sync();
//        } finally {
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//        }
//    }
//}
