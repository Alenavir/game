package ru.alenavir.eventservice.service.netty.handler.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.UnitDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.CommandHandler;
import ru.alenavir.eventservice.service.netty.handler.unit.command.UnitMoveCommand;

import java.util.Set;

/**
 * Обрабатывать команду перемещения юнита (UNIT_MOVED)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UnitMoveHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final NettyServer nettyServer;

    @Override
    public String getCommandType() {
        return UnitCommandType.UNIT_MOVE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            UnitMoveCommand cmd = objectMapper.treeToValue(payload, UnitMoveCommand.class);

            Set<ConstraintViolation<UnitMoveCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            UnitDto response = client.moveUnit(
                    cmd.x(),
                    cmd.y(),
                    cmd.playerId(),
                    cmd.unitId()
            );

            log.info("Перемещение юнита {} игроком {} в ({}, {})",
                    response.id(), response.playerId(), response.x(), response.y());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", response.gameId());
            payloadNode.put("playerId", response.playerId());
            payloadNode.put("x", response.x());
            payloadNode.put("y", response.y());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "UNIT_MOVED_RESPONSE");
            wrapper.set("payload", payloadNode);

            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    response.gameId().toString(),
                    payloadNode,
                    "UNIT_MOVED_BROADCAST",
                    ctx.channel()
            );


        } catch (Exception e) {
            log.error("Ошибка при обработке UNIT_MOVED", e);
            sendError(ctx, "Failed to move unit");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<UnitMoveCommand>> violations) {

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
