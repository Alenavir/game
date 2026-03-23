package ru.alenavir.gameservice.service.kafka.topic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventRouter {

    private final List<EventRoutingStrategy> strategies;

    public List<String> resolveTopics(String type) {
        return strategies.stream()
                .filter(s -> s.supports(type))
                .flatMap(s -> s.getTopics().stream())
                .distinct()
                .toList();
    }
}