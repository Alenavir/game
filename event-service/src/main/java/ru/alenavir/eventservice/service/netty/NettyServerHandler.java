package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


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

    private String gameId;

    public NettyServerHandler(NettyServer nettyServer,
                              ObjectMapper objectMapper,
                              CommandDispatcher dispatcher) {
        this.nettyServer = nettyServer;
        this.objectMapper = objectMapper;
        this.dispatcher = dispatcher;
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

            // привязка к игре
            if (payload.has("gameId") && gameId == null) { // только при первом присоединении
                gameId = payload.get("gameId").asText();
                nettyServer.addChannel(gameId, ctx.channel());
                log.info("Клиент присоединился к игре {}", gameId);
            }

            dispatcher.dispatch(commandType, payload, ctx);

        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения", e);
            sendError(ctx, "Invalid JSON format");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (gameId != null) {
            nettyServer.removeChannel(gameId, ctx.channel());
            log.info("Клиент покинул игру {}", gameId);
        }
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
