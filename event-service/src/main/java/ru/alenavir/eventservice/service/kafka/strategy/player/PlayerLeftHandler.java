package ru.alenavir.eventservice.service.kafka.strategy.player;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.events.AggregateType;
import ru.alenavir.eventservice.dto.events.player.PlayerLeftEvent;
import ru.alenavir.eventservice.dto.events.player.enums.PlayerEventType;
import ru.alenavir.eventservice.service.kafka.strategy.BaseEventHandler;

@Component
@Slf4j
public class PlayerLeftHandler implements BaseEventHandler<PlayerLeftEvent> {

    @Override
    public AggregateType getEventType() {
        return AggregateType.PLAYER;
    }

    @Override
    public Class<PlayerLeftEvent> getEventClass() {
        return PlayerLeftEvent.class;
    }

    @Override
    public String getExpectedEventTypeName() {
        return PlayerEventType.PLAYER_LEFT_GAME.name();
    }

    @Override
    public void handle(PlayerLeftEvent event) {
        log.info("PLAYER_LEFT_GAME: playerId={}, gameId={}", event.playerId(), event.gameId());
    }
}
