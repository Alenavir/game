package ru.alenavir.playerservice.dto.events.player;

public record PlayerJoinedEvent(Long playerId, Long gameId) implements PlayerEvent {
}