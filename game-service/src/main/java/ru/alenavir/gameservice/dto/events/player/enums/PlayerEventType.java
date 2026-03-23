package ru.alenavir.gameservice.dto.events.player.enums;

import ru.alenavir.gameservice.entity.enums.EventType;

public enum PlayerEventType implements EventType {
    PLAYER_JOINED_GAME,
    PLAYER_LEFT_GAME;
}