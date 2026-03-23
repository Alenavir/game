package ru.alenavir.unitservice.service.kafka.topic;

import org.springframework.stereotype.Component;
import ru.alenavir.unitservice.entity.enums.EventType;

import java.util.List;

@Component
public class EventStrategy implements EventRoutingStrategy {

    @Override
    public boolean supports(String eventType) {
        return eventType.startsWith("UNIT_");
    }

    @Override
    public List<String> getTopics() {
        return List.of("event-events");
    }
    
}
