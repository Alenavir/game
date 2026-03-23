package ru.alenavir.gameservice.dto.events;

public record EventEnvelope(
        String eventType,
        String aggregateType,
        String payload
) {}
