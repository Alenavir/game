package ru.alenavir.eventservice.dto.events.unit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UnitMovedEvent(
        Long gameId,
        Long unitId,
        double x,
        double y
) implements UnitEvent {
}