package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.grpc.EventGrpcClient;

import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Запускает Netty сервер при старте приложения
 * Поднимает порт и инициализирует pipeline
 */
@Component
@Slf4j
public class NettyServerStarter {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final CommandDispatcher commandDispatcher;
    private final ObjectMapper objectMapper;
    private final NettyServer nettyServer;
    private final EventGrpcClient grpcClient;
    private final ScheduledExecutorService disconnectScheduler;

    public NettyServerStarter(CommandDispatcher commandDispatcher,
                              ObjectMapper objectMapper,
                              NettyServer nettyServer,
                              EventGrpcClient grpcClient,
                              @Qualifier("disconnectScheduler") ScheduledExecutorService disconnectScheduler) {
        this.commandDispatcher = commandDispatcher;
        this.objectMapper = objectMapper;
        this.nettyServer = nettyServer;
        this.grpcClient = grpcClient;
        this.disconnectScheduler = disconnectScheduler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startNetty() {
        bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        new Thread(() -> {
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new NettyServerInitializer(
                                nettyServer, objectMapper, commandDispatcher, grpcClient, disconnectScheduler));

                ChannelFuture f = b.bind(8081).sync();
                log.info("Netty сервер запущен на порту 8081");
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Netty сервер прерван", e);
                Thread.currentThread().interrupt();
            }
        }, "Netty-Thread").start();
    }

    @PreDestroy
    public void stopNetty() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        log.info("Netty сервер остановлен");
    }
}