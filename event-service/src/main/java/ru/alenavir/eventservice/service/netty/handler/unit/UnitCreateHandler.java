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
import ru.alenavir.eventservice.service.netty.handler.unit.command.UnitCreateCommand;
import ru.alenavir.unitservice.grpc.UnitServiceProto;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class UnitCreateHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final NettyServer nettyServer;

    @Override
    public String getCommandType() {
        return UnitCommandType.UNIT_CREATE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            UnitCreateCommand cmd = objectMapper.treeToValue(payload, UnitCreateCommand.class);

            Set<ConstraintViolation<UnitCreateCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

            UnitServiceProto.UnitType grpcType = UnitServiceProto.UnitType.valueOf(cmd.type().name());

            UnitDto response = client.createUnit(
                    grpcType,
                    cmd.x(),
                    cmd.y(),
                    cmd.playerId(),
                    cmd.gameId()
            );

            log.info("Создание юнита {} игроком {} в ({}, {}) типа {}",
                    response.id(), response.playerId(), response.x(), response.y(), response.type());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", response.gameId());
            payloadNode.put("playerId", response.playerId());
            payloadNode.put("unitId", response.id());
            payloadNode.put("unitType", response.type().name());
            payloadNode.put("x", response.x());
            payloadNode.put("y", response.y());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "UNIT_CREATED_RESPONSE");
            wrapper.set("payload", payloadNode);

            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    cmd.gameId().toString(),
                    payloadNode,
                    "UNIT_CREATED_BROADCAST",
                    ctx.channel()
            );

        } catch (Exception e) {
            log.error("Ошибка при обработке UNIT_CREATED", e);
            sendError(ctx, "Failed to create unit");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<UnitCreateCommand>> violations) {

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
