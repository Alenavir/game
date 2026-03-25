package ru.alenavir.eventservice.service.netty.handler.unit.command;

public record UnitAttackCommand(
        Long attackerId,
        Long targetId,
        Long playerId
) {
}
