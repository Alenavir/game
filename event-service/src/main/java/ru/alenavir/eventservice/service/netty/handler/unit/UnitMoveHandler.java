package ru.alenavir.eventservice.service.netty.handler.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.UnitDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.BaseCommandHandler;
import ru.alenavir.eventservice.service.netty.handler.unit.command.UnitMoveCommand;

import java.util.concurrent.ExecutorService;

/**
 * Обрабатывать команду перемещения юнита (UNIT_MOVED)
 */
@Component
@Slf4j
public class UnitMoveHandler extends BaseCommandHandler {

    private final EventGrpcClient client;

    public UnitMoveHandler(ObjectMapper objectMapper,
                           Validator validator,
                           NettyServer nettyServer,
                           @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor,
                           EventGrpcClient client) {
        super(objectMapper, validator, nettyServer, nettyBusinessExecutor);
        this.client = client;
    }

    @Override
    public String getCommandType() {
        return UnitCommandType.UNIT_MOVE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        UnitMoveCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, UnitMoveCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать UnitMoveCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
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
        });
    }
}