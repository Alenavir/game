package ru.alenavir.eventservice.service.netty.handler.game.command;

public record ReconnectCommand (
    Long playerId,
    Long gameId
){
}
