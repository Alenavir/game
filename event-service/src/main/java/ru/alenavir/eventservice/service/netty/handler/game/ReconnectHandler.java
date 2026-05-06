package ru.alenavir.eventservice.service.netty.handler.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import ru.alenavir.eventservice.service.netty.handler.game.command.ReconnectCommand;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class ReconnectHandler extends BaseCommandHandler {

    private final EventGrpcClient client;

    public ReconnectHandler(ObjectMapper objectMapper,
                            Validator validator,
                            NettyServer nettyServer,
                            @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor,
                            EventGrpcClient client) {
        super(objectMapper, validator, nettyServer, nettyBusinessExecutor);
        this.client = client;
    }

    @Override
    public String getCommandType() {
        return GameCommandType.RECONNECT.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        ReconnectCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, ReconnectCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать ReconnectCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
            GameDto game = client.getGame(cmd.gameId());

            if (!game.playerIds().contains(cmd.playerId())) {
                log.warn("Игрок {} не найден в игре {}", cmd.playerId(), cmd.gameId());
                sendError(ctx, "Player is not in this game");
                return;
            }

            onGameJoined(ctx, cmd.gameId().toString(), cmd.playerId()); // ← централизованно

            log.info("Игрок {} переподключился к игре {}", cmd.playerId(), cmd.gameId());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", game.id());
            payloadNode.put("state", game.state().name());

            ArrayNode players = payloadNode.putArray("players");
            game.playerIds().forEach(players::add);

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "RECONNECT_RESPONSE");
            wrapper.set("payload", payloadNode);
            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    cmd.gameId().toString(),
                    payloadNode,
                    "PLAYER_RECONNECTED_BROADCAST",
                    ctx.channel()
            );
        });
    }
}