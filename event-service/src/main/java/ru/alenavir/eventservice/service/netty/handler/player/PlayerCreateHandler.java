package ru.alenavir.eventservice.service.netty.handler.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.PlayerDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.handler.CommandHandler;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayerCreateHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    public String getCommandType() {
        return PlayerCommandType.PLAYER_CREATE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            PlayerCreateCommand cmd = objectMapper.treeToValue(payload, PlayerCreateCommand.class);

            Set<ConstraintViolation<PlayerCreateCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            PlayerDto response = client.createPlayer(cmd.name());

            log.info("Создание игрока {} с именем {}", response.id(), response.name());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("playerId", response.id());
            payloadNode.put("name", response.name());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "PLAYER_CREATED_RESPONSE");
            wrapper.set("payload", payloadNode);

            ctx.writeAndFlush(wrapper + "\n");

        } catch (Exception e) {
            log.error("Ошибка при обработке PLAYER_CREATED", e);
            sendError(ctx, "Failed to create player");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<PlayerCreateCommand>> violations) {

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
