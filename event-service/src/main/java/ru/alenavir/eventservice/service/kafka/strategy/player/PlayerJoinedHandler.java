package ru.alenavir.eventservice.service.kafka.strategy.player;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.events.AggregateType;
import ru.alenavir.eventservice.dto.events.player.PlayerJoinedEvent;
import ru.alenavir.eventservice.dto.events.player.enums.PlayerEventType;
import ru.alenavir.eventservice.service.kafka.strategy.BaseEventHandler;

@Component
@Slf4j
public class PlayerJoinedHandler implements BaseEventHandler<PlayerJoinedEvent> {

    @Override
    public AggregateType getEventType() {
        return AggregateType.PLAYER;
    }

    @Override
    public Class<PlayerJoinedEvent> getEventClass() {
        return PlayerJoinedEvent.class;
    }

    @Override
    public String getExpectedEventTypeName() {
        return PlayerEventType.PLAYER_JOINED_GAME.name();
    }

    @Override
    public void handle(PlayerJoinedEvent event) {
        log.info("PLAYER_JOINED_GAME: playerId={}, gameId={}",
                event.playerId(), event.gameId());
    }
}
