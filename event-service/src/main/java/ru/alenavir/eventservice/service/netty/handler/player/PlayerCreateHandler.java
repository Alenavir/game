package ru.alenavir.eventservice.service.netty.handler.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.PlayerDto;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.handler.BaseCommandHandler;

import java.util.concurrent.ExecutorService;


@Component
@Slf4j
public class PlayerCreateHandler extends BaseCommandHandler {

    private final EventGrpcClient client;

    public PlayerCreateHandler(ObjectMapper objectMapper,
                               Validator validator,
                               @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor,
                               EventGrpcClient client) {
        super(objectMapper, validator, nettyBusinessExecutor);
        this.client = client;
    }

    @Override
    public String getCommandType() {
        return PlayerCommandType.PLAYER_CREATE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        PlayerCreateCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, PlayerCreateCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать PlayerCreateCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
            PlayerDto response = client.createPlayer(cmd.name());

            log.info("Игрок создан: id={}, name={}", response.id(), response.name());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("playerId", response.id());
            payloadNode.put("name", response.name());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "PLAYER_CREATED_RESPONSE");
            wrapper.set("payload", payloadNode);

            ctx.writeAndFlush(wrapper + "\n");
        });
    }
}
