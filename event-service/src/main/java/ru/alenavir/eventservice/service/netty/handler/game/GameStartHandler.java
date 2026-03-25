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
import ru.alenavir.eventservice.service.netty.handler.CommandHandler;
import ru.alenavir.eventservice.service.netty.handler.game.command.StartGameCommand;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameStartHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    public String getCommandType() {
        return GameCommandType.GAME_START.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            StartGameCommand cmd = objectMapper.treeToValue(payload, StartGameCommand.class);

            Set<ConstraintViolation<StartGameCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            GameDto response = client.startGame(cmd.gameId());

            log.info("Игра с id = {} началась", response.id());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", response.id());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "GAME_STARTED_RESPONSE");
            wrapper.set("payload", payloadNode);

            ctx.writeAndFlush(wrapper + "\n");

        } catch (Exception e) {
            log.error("Ошибка при обработке GAME_STARTED", e);
            sendError(ctx, "Failed to start game");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<StartGameCommand>> violations) {

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
