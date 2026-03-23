package ru.alenavir.eventservice.dto.events.unit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ru.alenavir.eventservice.dto.events.unit.enums.UnitType;

@JsonIgnoreProperties(ignoreUnknown = true)
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