package ru.alenavir.eventservice.dto.events.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerJoinedEvent(Long playerId, Long gameId) implements PlayerEvent {
}
