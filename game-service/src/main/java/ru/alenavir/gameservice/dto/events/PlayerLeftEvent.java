package ru.alenavir.gameservice.dto.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PlayerLeftEvent implements GameEvent {
    private Long playerId;
    private String eventType = "PLAYER_LEFT";

    public PlayerLeftEvent(Long playerId) {
        this.playerId = playerId;
    }
}
