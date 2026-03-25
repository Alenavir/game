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
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.CommandHandler;
import ru.alenavir.eventservice.service.netty.handler.game.command.PlayerLeaveCommand;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerLeaveHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final NettyServer nettyServer;

    @Override
    public String getCommandType() {
        return GameCommandType.PLAYER_LEAVE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            PlayerLeaveCommand cmd = objectMapper.treeToValue(payload, PlayerLeaveCommand.class);

            Set<ConstraintViolation<PlayerLeaveCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            client.leaveGame(cmd.playerId(), cmd.gameId());

            log.info("Игрок с id = {} вышел из игры {}", cmd.playerId(), cmd.gameId());

            nettyServer.removeChannel(cmd.gameId().toString(), ctx.channel());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", cmd.gameId());
            payloadNode.put("playerId", cmd.playerId());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "PLAYER_LEFT_RESPONSE");
            wrapper.set("payload", payloadNode);

            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    cmd.gameId().toString(),
                    payloadNode,
                    "PLAYER_LEFT_BROADCAST",
                    ctx.channel()
            );

        } catch (Exception e) {
            log.error("Ошибка при обработке PLAYER_LEFT", e);
            sendError(ctx, "Failed to leave player");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<PlayerLeaveCommand>> violations) {

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
