package ru.alenavir.playerservice.service.kafka.strategy.player;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.playerservice.dto.events.AggregateType;
import ru.alenavir.playerservice.dto.events.player.PlayerJoinedEvent;
import ru.alenavir.playerservice.dto.events.player.enums.PlayerEventType;
import ru.alenavir.playerservice.service.PlayerService;
import ru.alenavir.playerservice.service.kafka.BaseEventHandler;


@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerJoinedHandler implements BaseEventHandler<PlayerJoinedEvent> {

    private final PlayerService service;

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
        service.assignToGame(event.playerId(), event.gameId());
    }
}

