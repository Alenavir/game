package ru.alenavir.gameservice.dto.events.player;

public record PlayerJoinedEvent(Long playerId, Long gameId) implements PlayerEvent {

}
