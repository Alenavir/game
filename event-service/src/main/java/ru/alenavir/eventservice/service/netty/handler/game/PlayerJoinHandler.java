package ru.alenavir.eventservice.service.netty.handler.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.GameDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.CommandHandler;
import ru.alenavir.eventservice.service.netty.handler.game.command.PlayerJoinCommand;

import java.util.Set;

// PlayerJoinHandler.java
@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerJoinHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final NettyServer nettyServer;

    @Override
    public String getCommandType() {
        return GameCommandType.PLAYER_JOIN.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            PlayerJoinCommand cmd = objectMapper.treeToValue(payload, PlayerJoinCommand.class);

            Set<ConstraintViolation<PlayerJoinCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            client.joinGame(cmd.playerId(), cmd.gameId());
            log.info("Игрок {} присоединился к игре {}", cmd.playerId(), cmd.gameId());

            nettyServer.addChannel(cmd.gameId().toString(), ctx.channel());

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

        } catch (Exception e) {
            log.error("Ошибка при обработке PLAYER_JOINED", e);
            sendError(ctx, "Failed to join game");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<PlayerJoinCommand>> violations) {
        String message = violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .findFirst()
                .orElse("Validation error");
        sendError(ctx, message);
    }

    private void sendError(ChannelHandlerContext ctx, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", message);
        ctx.writeAndFlush(error + "\n");
    }
}