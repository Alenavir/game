package ru.alenavir.eventservice.service.netty.handler.unit.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UnitCreateCommand(
        @NotNull UnitType type,
        @NotNull Long gameId,
        @NotNull Long playerId,
        @Min(0) double x,
        @Min(0) double y
) {}