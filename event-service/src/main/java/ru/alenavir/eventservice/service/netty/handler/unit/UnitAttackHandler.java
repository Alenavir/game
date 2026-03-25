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
import ru.alenavir.eventservice.dto.AttackUnitDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.CommandHandler;
import ru.alenavir.eventservice.service.netty.handler.unit.command.UnitAttackCommand;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class UnitAttackHandler implements CommandHandler {

    private final EventGrpcClient client;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final NettyServer nettyServer;

    @Override
    public String getCommandType() {
        return UnitCommandType.UNIT_ATTACK.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        try {
            UnitAttackCommand cmd = objectMapper.treeToValue(payload, UnitAttackCommand.class);

            Set<ConstraintViolation<UnitAttackCommand>> violations = validator.validate(cmd);
            if (!violations.isEmpty()) {
                sendValidationError(ctx, violations);
                return;
            }

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

        } catch (Exception e) {
            log.error("Ошибка при обработке UNIT_ATTACKED", e);
            sendError(ctx, "Failed to attack unit");
        }
    }

    private void sendValidationError(ChannelHandlerContext ctx,
                                     Set<ConstraintViolation<UnitAttackCommand>> violations) {
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
