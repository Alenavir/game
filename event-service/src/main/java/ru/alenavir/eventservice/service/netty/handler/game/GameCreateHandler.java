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
import ru.alenavir.eventservice.service.netty.handler.game.command.CreateGameCommand;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameCreateHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final NettyServer nettyServer;

    @Override
    public String getCommandType() {
        return GameCommandType.GAME_CREATE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            CreateGameCommand cmd = objectMapper.treeToValue(payload, CreateGameCommand.class);

            Set<ConstraintViolation<CreateGameCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            GameDto response = client.createGame(cmd.playerId());

            nettyServer.addChannel(response.id().toString(), ctx.channel());

            log.info("Игра с id = {} создана игроком - {}", response, cmd.playerId());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", response.id());
            payloadNode.put("playerId", cmd.playerId());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "GAME_CREATED_RESPONSE");
            wrapper.set("payload", payloadNode);

            ctx.writeAndFlush(wrapper + "\n");

        } catch (Exception e) {
            log.error("Ошибка при обработке GAME_CREATED", e);
            sendError(ctx, "Failed to create game");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<CreateGameCommand>> violations) {

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
