package ru.alenavir.gameservice.dto.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PlayerJoinedEvent implements GameEvent {
    private Long playerId;
    private Long gameId;
    private String eventType = "PLAYER_JOINED";

    public PlayerJoinedEvent(Long playerId, Long gameId) {
        this.playerId = playerId;
        this.gameId = gameId;
    }
}