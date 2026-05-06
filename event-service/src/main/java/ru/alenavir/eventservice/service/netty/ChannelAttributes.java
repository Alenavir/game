package ru.alenavir.eventservice.service.netty;

import io.netty.util.AttributeKey;

public final class ChannelAttributes {
    public static final AttributeKey<Boolean> LEFT_GRACEFULLY = AttributeKey.valueOf("leftGracefully");
    public static final AttributeKey<Long> PLAYER_ID = AttributeKey.valueOf("playerId");
    public static final AttributeKey<String> GAME_ID = AttributeKey.valueOf("gameId");

    private ChannelAttributes() {}
}
