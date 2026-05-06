package ru.alenavir.eventservice.service.netty.handler.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.AttackUnitDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.BaseCommandHandler;
import ru.alenavir.eventservice.service.netty.handler.unit.command.UnitAttackCommand;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class UnitAttackHandler extends BaseCommandHandler {

    private final EventGrpcClient client;

    public UnitAttackHandler(ObjectMapper objectMapper,
                             Validator validator,
                             NettyServer nettyServer,
                             @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor,
                             EventGrpcClient client) {
        super(objectMapper, validator, nettyServer, nettyBusinessExecutor);
        this.client = client;
    }

    @Override
    public String getCommandType() {
        return UnitCommandType.UNIT_ATTACK.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        UnitAttackCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, UnitAttackCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать UnitAttackCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
            AttackUnitDto response = client.attackUnit(
                    cmd.playerId(),
                    cmd.targetId(),
                    cmd.attackerId()
            );

            log.info("Атака: attackerId={}, targetId={}, playerId={}, damage={}, targetDead={}",
                    response.attacker().id(),
                    response.target() != null ? response.target().id() : null,
                    response.attacker().playerId(),
                    response.damage(),
                    response.targetDead()
            );

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("targetId", response.target().id());
            payloadNode.put("playerId", response.attacker().playerId());
            payloadNode.put("attackerId", response.attacker().id());
            payloadNode.put("gameId", response.attacker().gameId());
            payloadNode.put("damage", response.damage());
            payloadNode.put("targetDead", response.targetDead());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "UNIT_ATTACKED_RESPONSE");
            wrapper.set("payload", payloadNode);
            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    response.attacker().gameId().toString(),
                    payloadNode,
                    "UNIT_ATTACKED_BROADCAST",
                    ctx.channel()
            );
        });
    }
}