package ru.alenavir.gameservice.service.kafka.topic;

import org.springframework.stereotype.Component;
import ru.alenavir.gameservice.entity.enums.EventType;

@Component
public class PlayerStrategy implements EventRoutingStrategy {

    @Override
    public boolean supports(EventType eventType) {
//        return eventType == EventType.PLAYER_JOINED_GAME;
        return eventType.name().startsWith("PLAYER_");
    }

    @Override
    public String getTopic() {
        return "player-events";
    }
}
