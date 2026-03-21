package ru.alenavir.unitservice.service.kafka.topic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.alenavir.unitservice.entity.enums.EventType;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventRouter {

    private final List<EventRoutingStrategy> strategies;

    public String resolveTopic(EventType type) {
        return strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .map(EventRoutingStrategy::getTopic)
                .orElseThrow(() -> new RuntimeException("No topic for " + type));
    }
}