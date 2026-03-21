package ru.alenavir.unitservice.service.kafka.topic;

import org.springframework.stereotype.Component;
import ru.alenavir.unitservice.entity.enums.EventType;

@Component
public class EventStrategy implements EventRoutingStrategy {

    @Override
    public boolean supports(EventType eventType) {
//        return eventType == EventType.PLAYER_JOINED_GAME;
        return eventType.name().startsWith("EVENT_");
    }

    @Override
    public String getTopic() {
        return "event-events";
    }
}
