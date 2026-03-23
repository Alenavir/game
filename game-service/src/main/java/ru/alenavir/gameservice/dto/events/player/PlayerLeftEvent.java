package ru.alenavir.gameservice.dto.events.player;

public record PlayerLeftEvent(Long playerId, Long gameId) implements PlayerEvent {
}