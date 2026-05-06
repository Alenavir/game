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
import ru.alenavir.eventservice.service.netty.handler.game.command.CreateGameCommand;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class GameCreateHandler extends BaseCommandHandler {

    private final EventGrpcClient client;

    public GameCreateHandler(ObjectMapper objectMapper,
                             Validator validator,
                             NettyServer nettyServer,
                             @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor,
                             EventGrpcClient client) {
        super(objectMapper, validator, nettyServer, nettyBusinessExecutor);
        this.client = client;
    }

    @Override
    public String getCommandType() {
        return GameCommandType.GAME_CREATE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        CreateGameCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, CreateGameCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать CreateGameCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
            GameDto response = client.createGame(cmd.playerId());

            onGameJoined(ctx, response.id().toString(), cmd.playerId()); // ← централизованно

            log.info("Игра с id={} создана игроком {}", response.id(), cmd.playerId());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", response.id());
            payloadNode.put("playerId", cmd.playerId());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "GAME_CREATED_RESPONSE");
            wrapper.set("payload", payloadNode);
            ctx.writeAndFlush(wrapper + "\n");
        });
    }
}
