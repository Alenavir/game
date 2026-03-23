package ru.alenavir.eventservice.service.kafka.strategy.unit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.events.AggregateType;
import ru.alenavir.eventservice.dto.events.unit.UnitCreatedEvent;
import ru.alenavir.eventservice.dto.events.unit.enums.UnitEventType;
import ru.alenavir.eventservice.service.kafka.strategy.BaseEventHandler;

@Component
@Slf4j
public class UnitCreatedHandler implements BaseEventHandler<UnitCreatedEvent> {

    @Override
    public AggregateType getEventType() {
        return AggregateType.UNIT;
    }

    @Override
    public Class<UnitCreatedEvent> getEventClass() {
        return UnitCreatedEvent.class;
    }

    @Override
    public String getExpectedEventTypeName() {
        return UnitEventType.UNIT_CREATED.name();
    }

    @Override
    public void handle(UnitCreatedEvent event) {
        log.info("UNIT_CREATED: unitId={}, ownerId={}, gameId={}", event.unitId(), event.ownerId(), event.gameId());
    }
}
