package ru.alenavir.eventservice.service.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.alenavir.eventservice.service.netty.ChannelAttributes;
import ru.alenavir.eventservice.service.netty.NettyServer;

import java.util.Set;
import java.util.concurrent.ExecutorService;

@Slf4j
public abstract class BaseCommandHandler implements CommandHandler {

    protected final ObjectMapper objectMapper;
    protected final Validator validator;
    protected final NettyServer nettyServer;
    private final ExecutorService nettyBusinessExecutor;

    protected BaseCommandHandler(ObjectMapper objectMapper,
                                 Validator validator,
                                 NettyServer nettyServer,
                                 @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.nettyServer = nettyServer;
        this.nettyBusinessExecutor = nettyBusinessExecutor;
    }

    protected <T> Set<ConstraintViolation<T>> validate(T cmd) {
        return validator.validate(cmd);
    }

    protected void executeAsync(ChannelHandlerContext ctx, Runnable task) {
        nettyBusinessExecutor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Ошибка в asyncTask", e);
                sendError(ctx, "Internal server error");
            }
        });
    }

    // вызывать после успешного входа в игру
    protected void onGameJoined(ChannelHandlerContext ctx, String gameId, Long playerId) {
        nettyServer.addChannel(gameId, ctx.channel());
        ctx.channel().attr(ChannelAttributes.GAME_ID).set(gameId);
        ctx.channel().attr(ChannelAttributes.PLAYER_ID).set(playerId);
        log.info("Игрок {} привязан к игре {}", playerId, gameId);
    }

    protected void sendError(ChannelHandlerContext ctx, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", message);
        ctx.writeAndFlush(error + "\n");
    }

    protected void sendValidationError(ChannelHandlerContext ctx,
                                       Set<? extends ConstraintViolation<?>> violations) {
        String message = violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .findFirst()
                .orElse("Validation error");
        sendError(ctx, message);
    }
}