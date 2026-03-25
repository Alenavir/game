package ru.alenavir.eventservice.service.netty.handler.game.command;

import jakarta.validation.constraints.NotNull;

public record PlayerJoinCommand(
        @NotNull Long playerId,
        @NotNull Long gameId
) {
}
