package ru.alenavir.eventservice.service.netty.handler.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.grpc.EventGrpcClient;
import ru.alenavir.eventservice.service.netty.ChannelAttributes;
import ru.alenavir.eventservice.service.netty.NettyServer;
import ru.alenavir.eventservice.service.netty.handler.BaseCommandHandler;
import ru.alenavir.eventservice.service.netty.handler.game.command.PlayerLeaveCommand;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class PlayerLeaveHandler extends BaseCommandHandler {

    private final EventGrpcClient client;
    private final NettyServer nettyServer;

    public PlayerLeaveHandler(ObjectMapper objectMapper,
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
        return GameCommandType.PLAYER_LEAVE.name();
    }

    @Override
    public void handle(JsonNode payload, ChannelHandlerContext ctx) {
        PlayerLeaveCommand cmd;
        try {
            cmd = objectMapper.treeToValue(payload, PlayerLeaveCommand.class);
        } catch (Exception e) {
            log.warn("Не удалось десериализовать PlayerLeaveCommand: {}", e.getMessage());
            sendError(ctx, "Invalid payload");
            return;
        }

        var violations = validate(cmd);
        if (!violations.isEmpty()) {
            sendValidationError(ctx, violations);
            return;
        }

        executeAsync(ctx, () -> {
            client.leaveGame(cmd.playerId(), cmd.gameId());

            ctx.channel().attr(ChannelAttributes.LEFT_GRACEFULLY).set(true);

            log.info("Игрок {} вышел из игры {}", cmd.playerId(), cmd.gameId());

            nettyServer.removeChannel(cmd.gameId().toString(), ctx.channel());

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("gameId", cmd.gameId());
            payloadNode.put("playerId", cmd.playerId());

            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("type", "PLAYER_LEFT_RESPONSE");
            wrapper.set("payload", payloadNode);
            ctx.writeAndFlush(wrapper + "\n");

            nettyServer.broadcastToGame(
                    cmd.gameId().toString(),
                    payloadNode,
                    "PLAYER_LEFT_BROADCAST",
                    ctx.channel()
            );
        });
    }
}