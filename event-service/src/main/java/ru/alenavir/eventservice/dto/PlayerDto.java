package ru.alenavir.eventservice.dto;

public record PlayerDto(
        Long id,
        String name,
        Long currentGameId
) {
}
