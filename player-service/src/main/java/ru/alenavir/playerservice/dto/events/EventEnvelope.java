package ru.alenavir.playerservice.dto.events;

public record EventEnvelope(
        String eventType,
        String aggregateType,
        String payload
) {}
