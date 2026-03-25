package ru.alenavir.playerservice.dto.events.player;

public record PlayerLeftEvent(Long playerId, Long gameId) implements PlayerEvent {

}