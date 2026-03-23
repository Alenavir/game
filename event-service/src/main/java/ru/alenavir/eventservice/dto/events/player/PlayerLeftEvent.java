package ru.alenavir.eventservice.dto.events.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerLeftEvent(Long playerId, Long gameId) implements PlayerEvent {

}
