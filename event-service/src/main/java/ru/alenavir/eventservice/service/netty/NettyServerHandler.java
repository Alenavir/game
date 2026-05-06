package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.alenavir.eventservice.grpc.EventGrpcClient;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Обрабатывает входящие сообщения от клиента
 * Делает:
 * - парсит JSON
 * - проверяет commandType
 * - привязывает клиента к gameId
 * - вызывает CommandDispatcher
 * - удаляет канал при отключении
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    private final NettyServer nettyServer;
    private final ObjectMapper objectMapper;
    private final CommandDispatcher dispatcher;
    private final EventGrpcClient grpcClient;
    private final ScheduledExecutorService scheduler;

    public NettyServerHandler(NettyServer nettyServer,
                              ObjectMapper objectMapper,
                              CommandDispatcher dispatcher,
                              EventGrpcClient grpcClient,
                              ScheduledExecutorService scheduler) {
        this.nettyServer = nettyServer;
        this.objectMapper = objectMapper;
        this.dispatcher = dispatcher;
        this.grpcClient = grpcClient;
        this.scheduler = scheduler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        try {
            JsonNode node = objectMapper.readTree(msg);

            if (!node.has("commandType") || !node.has("payload")) {
                log.warn("Некорректное сообщение: {}", msg);
                sendError(ctx, "Invalid JSON format");
                return;
            }

            String commandType = node.get("commandType").asText();
            JsonNode payload = node.get("payload");

            if (payload.has("gameId") && ctx.channel().attr(ChannelAttributes.GAME_ID).get() == null) {
                ctx.channel().attr(ChannelAttributes.GAME_ID).set(payload.get("gameId").asText());
            }

            if (payload.has("playerId") && ctx.channel().attr(ChannelAttributes.PLAYER_ID).get() == null) {
                ctx.channel().attr(ChannelAttributes.PLAYER_ID).set(payload.get("playerId").asLong());
            }

            if ("RECONNECT".equals(commandType) && payload.has("playerId")) {
                nettyServer.cancelDisconnect(payload.get("playerId").asLong());
            }

            dispatcher.dispatch(commandType, payload, ctx);

        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения", e);
            sendError(ctx, "Invalid JSON format");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String gameId = ctx.channel().attr(ChannelAttributes.GAME_ID).get();
        Long playerId = ctx.channel().attr(ChannelAttributes.PLAYER_ID).get();

        if (gameId == null || playerId == null) return;

        nettyServer.removeChannel(gameId, ctx.channel());

        Boolean leftGracefully = ctx.channel().attr(ChannelAttributes.LEFT_GRACEFULLY).get();
        if (Boolean.TRUE.equals(leftGracefully)) {
            log.info("Игрок {} вышел из игры {} сам, таймер не запускаем", playerId, gameId);
            return;
        }

        log.info("Клиент {} отключился от игры {}, запускаем таймер реконнекта", playerId, gameId);

        ScheduledFuture<?> task = scheduler.schedule(() -> {
            try {
                log.info("Игрок {} не переподключился, удаляем из игры {}", playerId, gameId);
                grpcClient.leaveGame(playerId, Long.parseLong(gameId));
                nettyServer.broadcastToGame(
                        gameId,
                        java.util.Map.of("playerId", playerId, "gameId", gameId),
                        "PLAYER_DISCONNECTED_BROADCAST",
                        null
                );
            } catch (Exception e) {
                log.error("Ошибка при удалении отключившегося игрока {}", playerId, e);
            } finally {
                nettyServer.removeDisconnectTask(playerId); // тихая чистка
            }
        }, 30, TimeUnit.SECONDS);

        nettyServer.scheduleDisconnect(playerId, task);
    }

    private void sendError(ChannelHandlerContext ctx, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", message);
        ctx.writeAndFlush(error + "\n");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Ошибка Netty", cause);
        ctx.close();
    }
}