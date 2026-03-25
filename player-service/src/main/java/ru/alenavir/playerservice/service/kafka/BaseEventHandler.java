package ru.alenavir.playerservice.service.kafka;

import ru.alenavir.playerservice.dto.events.AggregateType;

public interface BaseEventHandler<T> {
    AggregateType getEventType(); // К какому агрегату относится (PLAYER, GAME, UNIT)
    Class<T> getEventClass();     // Класс события, который обрабатывает handler
    void handle(T event);         // Логика обработки
    String getExpectedEventTypeName();
}