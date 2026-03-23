package ru.alenavir.eventservice.dto.events.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameStartedEvent(Long gameId, List<Long> playerIds) implements GameEvent {

}