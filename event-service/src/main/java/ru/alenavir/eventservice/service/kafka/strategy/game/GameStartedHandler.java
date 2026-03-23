package ru.alenavir.eventservice.service.kafka.strategy.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.events.AggregateType;
import ru.alenavir.eventservice.dto.events.game.GameStartedEvent;
import ru.alenavir.eventservice.dto.events.game.enums.GameEventType;
import ru.alenavir.eventservice.dto.events.player.enums.PlayerEventType;
import ru.alenavir.eventservice.service.kafka.strategy.BaseEventHandler;

@Component
@Slf4j
public class GameStartedHandler implements BaseEventHandler<GameStartedEvent> {

    @Override
    public AggregateType getEventType() {
        return AggregateType.GAME;
    }

    @Override
    public Class<GameStartedEvent> getEventClass() {
        return GameStartedEvent.class;
    }

    @Override
    public String getExpectedEventTypeName() {
        return GameEventType.GAME_STARTED.name();
    }

    @Override
    public void handle(GameStartedEvent event) {
        log.info("GAME_STARTED: gameId={}, players={}", event.gameId(), event.playerIds());
    }
}