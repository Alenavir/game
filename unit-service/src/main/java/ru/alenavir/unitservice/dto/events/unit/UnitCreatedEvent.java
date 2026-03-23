package ru.alenavir.unitservice.dto.events.unit;

import ru.alenavir.unitservice.entity.enums.UnitType;

public record UnitCreatedEvent (
        Long gameId,
        Long unitId,
        Long ownerId,
        UnitType type,
        double x,
        double y,
        int health
) implements UnitEvent {

}