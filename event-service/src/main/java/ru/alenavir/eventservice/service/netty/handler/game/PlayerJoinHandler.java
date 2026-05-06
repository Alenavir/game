package ru.alenavir.eventservice.service.netty.handler.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.grpc.StatusRuntimeException;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.BaseCommandHandler;
import ru.alenavir.eventservice.service.netty.handler.game.command.PlayerJoinCommand;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class PlayerJoinHandler extends BaseCommandHandler {

    private final EventGrpcClient client;
    private final NettyServer nettyServer;

    public PlayerJoinHandler(ObjectMapper objectMapper,
                             Validator validator,
                             @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor,
                             EventGrpcClient client,
                             NettyServer nettyServer) {
        super(objectMapper, validator, nettyBusinessExecutor);
        this.client = client;
        this.nettyServer = nettyServer;
    }

    @Override
    public String getCommandType() {
        return GameCommandType.PLAYER_JOIN.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        PlayerJoinCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, PlayerJoinCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать PlayerJoinCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
            try {
                client.joinGame(cmd.playerId(), cmd.gameId());
            } catch (StatusRuntimeException e) {
                sendError(ctx, e.getStatus().getDescription());
                return;
            }

            log.info("Игрок {} присоединился к игре {}", cmd.playerId(), cmd.gameId());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", cmd.gameId());
            payloadNode.put("playerId", cmd.playerId());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "PLAYER_JOINED_RESPONSE");
            wrapper.set("payload", payloadNode);
            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    cmd.gameId().toString(),
                    payloadNode,
                    "PLAYER_JOINED_BROADCAST",
                    ctx.channel()
            );
        });
    }
}