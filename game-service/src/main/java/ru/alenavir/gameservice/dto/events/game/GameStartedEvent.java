package ru.alenavir.gameservice.dto.events.game;

import java.util.List;

public record GameStartedEvent(Long gameId, List<Long> playerIds) implements GameEvent {
}