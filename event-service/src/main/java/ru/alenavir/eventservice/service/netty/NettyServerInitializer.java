package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import ru.alenavir.eventservice.grpc.EventGrpcClient;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Настраивает pipeline для каждого подключения
 * Добавляет:
 * - декодер (строка)
 * - энкодер
 * - NettyServerHandler
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServer nettyServer;
    private final ObjectMapper objectMapper;
    private final CommandDispatcher commandDispatcher;
    private final EventGrpcClient grpcClient;
    private final ScheduledExecutorService disconnectScheduler;

    public NettyServerInitializer(NettyServer nettyServer,
                                  ObjectMapper objectMapper,
                                  CommandDispatcher commandDispatcher,
                                  EventGrpcClient grpcClient,
                                  ScheduledExecutorService disconnectScheduler) {
        this.nettyServer = nettyServer;
        this.objectMapper = objectMapper;
        this.commandDispatcher = commandDispatcher;
        this.grpcClient = grpcClient;
        this.disconnectScheduler = disconnectScheduler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new LineBasedFrameDecoder(1024),
                new StringDecoder(),
                new StringEncoder(),
                new NettyServerHandler(nettyServer, objectMapper, commandDispatcher, grpcClient, disconnectScheduler)
        );
    }
}