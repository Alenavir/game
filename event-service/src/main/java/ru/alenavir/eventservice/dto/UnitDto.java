package ru.alenavir.eventservice.dto;

import ru.alenavir.eventservice.dto.enums.UnitType;

public record UnitDto(
        Long id,
        UnitType type,
        double x,
        double y,
        Long playerId,
        Long gameId,
        int health
) {}
