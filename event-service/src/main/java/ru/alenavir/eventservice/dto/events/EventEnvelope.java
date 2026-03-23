package ru.alenavir.eventservice.dto.events;

public record EventEnvelope(
        String eventType,
        String aggregateType,
        String payload
) {}
