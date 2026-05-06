package ru.alenavir.eventservice.service.netty.handler.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.GameDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.BaseCommandHandler;
import ru.alenavir.eventservice.service.netty.handler.game.command.StartGameCommand;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class GameStartHandler extends BaseCommandHandler {

    private final EventGrpcClient client;

    public GameStartHandler(ObjectMapper objectMapper,
                            Validator validator,
                            NettyServer nettyServer,
                            @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor,
                            EventGrpcClient client) {
        super(objectMapper, validator, nettyServer, nettyBusinessExecutor);
        this.client = client;
    }

    @Override
    public String getCommandType() {
        return GameCommandType.GAME_START.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        StartGameCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, StartGameCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать StartGameCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
            GameDto response = client.startGame(cmd.gameId());

            log.info("Игра с id={} началась", response.id());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", response.id());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "GAME_STARTED_RESPONSE");
            wrapper.set("payload", payloadNode);
            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    response.id().toString(),
                    payloadNode,
                    "GAME_STARTED_BROADCAST",
                    ctx.channel()
            );
        });
    }
}