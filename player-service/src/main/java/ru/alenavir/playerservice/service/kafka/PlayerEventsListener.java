package ru.alenavir.playerservice.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.alenavir.playerservice.dto.events.AggregateType;
import ru.alenavir.playerservice.dto.events.EventEnvelope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PlayerEventsListener {

    private final Map<AggregateType, Map<String, BaseEventHandler<?>>> handlerMap = new HashMap<>();
    private final ObjectMapper objectMapper;

    @Autowired
    public PlayerEventsListener(List<BaseEventHandler<?>> handlers,
                               ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        handlers.forEach(handler -> {
            AggregateType aggregateType = handler.getEventType();
            String eventTypeName = handler.getExpectedEventTypeName();

            handlerMap
                    .computeIfAbsent(aggregateType, k -> new HashMap<>())
                    .put(eventTypeName, handler);
        });
    }

    @KafkaListener(topics = "player-events", groupId = "player-service-group")
    public void onEvent(String message) {
        try {
            EventEnvelope envelope = objectMapper.readValue(message, EventEnvelope.class);

            AggregateType aggregateType = AggregateType.valueOf(envelope.aggregateType());
            String eventType = envelope.eventType();

            Map<String, BaseEventHandler<?>> handlersByEventType = handlerMap.get(aggregateType);
            if (handlersByEventType == null) {
                log.warn("Нет handler для aggregateType={}", aggregateType);
                return;
            }

            BaseEventHandler<?> handler = handlersByEventType.get(eventType);
            if (handler == null) {
                log.warn("Нет handler для eventType={} с aggregateType={}", eventType, aggregateType);
                return;
            }

            Object eventObj = objectMapper.readValue(envelope.payload(), handler.getEventClass());

            invokeHandler(handler, eventObj);

        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", message, e);
        }
    }

    private <T> void invokeHandler(BaseEventHandler<T> handler, Object eventObj) {
        @SuppressWarnings("unchecked")
        T event = (T) eventObj;
        handler.handle(event);
    }
}
