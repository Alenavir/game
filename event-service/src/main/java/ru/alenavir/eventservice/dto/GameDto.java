package ru.alenavir.eventservice.dto;

import ru.alenavir.eventservice.dto.enums.GameState;

import java.util.List;

public record GameDto(
        Long id,
        GameState state,
        List<Long> playerIds
) {}
