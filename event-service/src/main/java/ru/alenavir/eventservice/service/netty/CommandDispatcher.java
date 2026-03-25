package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.service.netty.handler.CommandHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Роутер команд
 * Делает:
 * - находит handler по commandType
 * - вызывает нужный handler
 * - отдаёт ошибку если команды нет
 */
@Component
@Slf4j
public class CommandDispatcher {

    private final ObjectMapper objectMapper;
    private final Map<String, CommandHandler> handlers = new HashMap<>();

    @Autowired
    public CommandDispatcher(List<CommandHandler> handlerList, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        handlerList.forEach(h -> handlers.put(h.getCommandType(), h));
        log.info("Зарегистрированы CommandHandlers: {}", handlers.keySet());
    }

    public void dispatch(String commandType, JsonNode payload, ChannelHandlerContext ctx) {
        CommandHandler handler = handlers.get(commandType);

        if (handler == null) {
            log.warn("Неизвестная команда: {}", commandType);

            ObjectNode error = objectMapper.createObjectNode();
            error.put("type", "ERROR");
            error.put("message", "Unknown command: " + commandType);

            ctx.writeAndFlush(error + "\n");
            return;
        }

        handler.handle(payload, ctx);
    }
}