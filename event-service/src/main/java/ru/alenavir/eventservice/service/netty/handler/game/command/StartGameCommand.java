package ru.alenavir.eventservice.service.netty.handler.game.command;

import jakarta.validation.constraints.NotNull;

public record StartGameCommand(
    @NotNull Long gameId
){}
