package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Запускает Netty сервер при старте приложения
 * Поднимает порт и инициализирует pipeline
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NettyServerStarter {

    private final CommandDispatcher commandDispatcher;
    private final ObjectMapper objectMapper;
    private final NettyServer nettyServer;

    @EventListener(ApplicationReadyEvent.class)
    public void startNetty() {
        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new NettyServerInitializer(nettyServer, objectMapper, commandDispatcher));

                ChannelFuture f = b.bind(8081).sync();
                log.info("Netty сервер запущен на порту 8081");
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Netty сервер прерван", e);
                Thread.currentThread().interrupt();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }, "Netty-Thread").start();
    }
}