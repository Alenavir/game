package ru.alenavir.eventservice.service.kafka.strategy.unit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.events.AggregateType;
import ru.alenavir.eventservice.dto.events.unit.UnitMovedEvent;
import ru.alenavir.eventservice.dto.events.unit.enums.UnitEventType;
import ru.alenavir.eventservice.service.kafka.strategy.BaseEventHandler;

@Component
@Slf4j
public class UnitMovedHandler implements BaseEventHandler<UnitMovedEvent> {

    @Override
    public AggregateType getEventType() {
        return AggregateType.UNIT;
    }

    @Override
    public Class<UnitMovedEvent> getEventClass() {
        return UnitMovedEvent.class;
    }

    @Override
    public String getExpectedEventTypeName() {
        return UnitEventType.UNIT_MOVED.name();
    }

    @Override
    public void handle(UnitMovedEvent event) {
        log.info("UNIT_MOVED: unitId={}, x={}, y={}", event.unitId(), event.x(), event.y());
    }
}
