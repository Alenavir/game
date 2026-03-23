package ru.alenavir.unitservice.dto.events;

public record EventEnvelope(
        String eventType,
        String aggregateType,
        String payload
) {}
