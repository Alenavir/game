package ru.alenavir.unitservice.dto.events.unit;

public record UnitMovedEvent (
        Long gameId,
        Long unitId,
        double x,
        double y
) implements UnitEvent {
}