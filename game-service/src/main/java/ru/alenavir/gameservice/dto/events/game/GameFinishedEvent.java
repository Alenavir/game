package ru.alenavir.gameservice.dto.events.game;

public record GameFinishedEvent(Long gameId) implements GameEvent {

}