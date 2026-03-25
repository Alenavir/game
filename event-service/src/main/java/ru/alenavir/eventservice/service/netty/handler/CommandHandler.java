package ru.alenavir.eventservice.service.netty.handler;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelHandlerContext;

/**
 * Абстракция обработчика команды
 */
public interface CommandHandler {
    // строка типа команды
    String getCommandType();

    // логика обработки
    void handle(JsonNode payload, ChannelHandlerContext ctx);
}
