package ru.alenavir.eventservice.service.kafka.strategy.game;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.events.AggregateType;
import ru.alenavir.eventservice.dto.events.game.GameFinishedEvent;
import ru.alenavir.eventservice.dto.events.game.enums.GameEventType;
import ru.alenavir.eventservice.dto.events.player.enums.PlayerEventType;
import ru.alenavir.eventservice.service.kafka.strategy.BaseEventHandler;

@Component
@Slf4j
public class GameFinishedHandler implements BaseEventHandler<GameFinishedEvent> {

    @Override
    public AggregateType getEventType() {
        return AggregateType.GAME;
    }

    @Override
    public Class<GameFinishedEvent> getEventClass() {
        return GameFinishedEvent.class;
    }

    @Override
    public String getExpectedEventTypeName() {
        return GameEventType.GAME_FINISHED.name();
    }

    @Override
    public void handle(GameFinishedEvent event) {
        log.info("GAME_FINISHED: gameId={}", event.gameId());
    }
}
