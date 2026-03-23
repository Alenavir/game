package ru.alenavir.gameservice.service.kafka.topic;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventStrategy implements EventRoutingStrategy {

    @Override
    public boolean supports(String eventType) {
        return eventType.startsWith("PLAYER_")
                || eventType.startsWith("GAME_");
    }

    @Override
    public List<String> getTopics() {
        return List.of("event-events");
    }

}
