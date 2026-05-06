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
import ru.alenavir.eventservice.service.netty.handler.unit.command.UnitCreateCommand;
import ru.alenavir.unitservice.grpc.UnitServiceProto;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class UnitCreateHandler extends BaseCommandHandler {

    private final EventGrpcClient client;
    private final NettyServer nettyServer;

    public UnitCreateHandler(ObjectMapper objectMapper,
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
        return UnitCommandType.UNIT_CREATE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        UnitCreateCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, UnitCreateCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать UnitCreateCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
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
        });
    }
}
