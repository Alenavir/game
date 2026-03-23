package ru.alenavir.eventservice.dto.events.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameFinishedEvent(Long gameId) implements GameEvent {
}