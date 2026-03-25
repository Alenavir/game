package ru.alenavir.eventservice.service.netty.handler.unit.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UnitMoveCommand(
    @NotNull Long unitId,
    @NotNull Long playerId,
    @Min(0) double x,
    @Min(0) double y
) {}
