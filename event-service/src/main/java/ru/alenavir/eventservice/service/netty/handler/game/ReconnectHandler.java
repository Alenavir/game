package ru.alenavir.eventservice.service.netty.handler.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import ru.alenavir.eventservice.service.netty.handler.game.command.ReconnectCommand;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReconnectHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final NettyServer nettyServer;

    @Override
    public String getCommandType() {
        return GameCommandType.RECONNECT.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            ReconnectCommand cmd = objectMapper.treeToValue(payload, ReconnectCommand.class);

            Set<ConstraintViolation<ReconnectCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            GameDto game = client.getGame(cmd.gameId());

            if (!game.playerIds().contains(cmd.playerId())) {
                sendError(ctx, "Player is not in this game");
                return;
            }

            nettyServer.addChannel(cmd.gameId().toString(), ctx.channel());

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

        } catch (Exception e) {
            log.error("Ошибка при RECONNECT", e);
            sendError(ctx, "Failed to reconnect");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<ReconnectCommand>> violations) {
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
