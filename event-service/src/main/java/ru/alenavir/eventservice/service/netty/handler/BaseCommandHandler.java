package ru.alenavir.eventservice.service.netty.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Set;
import java.util.concurrent.ExecutorService;

@Slf4j
public abstract class BaseCommandHandler implements CommandHandler {

    protected final ObjectMapper objectMapper;
    protected final Validator validator;
    private final ExecutorService nettyBusinessExecutor;

    protected BaseCommandHandler(ObjectMapper objectMapper,
                                 Validator validator,
                                 @Qualifier("nettyBusinessExecutor") ExecutorService nettyBusinessExecutor) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.nettyBusinessExecutor = nettyBusinessExecutor;
    }

    // валидация — быстрая, делается в Worker потоке
    protected <T> Set<ConstraintViolation<T>> validate(T cmd) {
        return validator.validate(cmd);
    }

    // тяжёлую логику в бизнес пуле
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