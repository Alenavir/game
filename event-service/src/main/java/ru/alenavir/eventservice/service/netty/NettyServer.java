package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Хранит все подключения игроков (gameId → channels)
 * Делает:
 * - add/remove каналов
 * - рассылку событий (broadcast) игрокам
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NettyServer {

    private final ObjectMapper objectMapper;

    private final Map<String, ConcurrentLinkedQueue<Channel>> gameChannels = new ConcurrentHashMap<>();

    public void addChannel(String gameId, Channel channel) {
        gameChannels
                .computeIfAbsent(gameId, k -> new ConcurrentLinkedQueue<>());

        ConcurrentLinkedQueue<Channel> channels = gameChannels.get(gameId);

        if (!channels.contains(channel)) {
            channels.add(channel);
        }
    }

    public void removeChannel(String gameId, Channel channel) {
        ConcurrentLinkedQueue<Channel> channels = gameChannels.get(gameId);
        if (channels != null) {
            channels.remove(channel);
            if (channels.isEmpty()) {
                gameChannels.remove(gameId);
            }
        }
    }

    public void broadcastToGame(String gameId, Object payload, String eventType, Channel exclude) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "eventType", eventType,
                    "payload", payload
            ));

            ConcurrentLinkedQueue<Channel> channels = gameChannels.get(gameId);

            if (channels != null) {
                channels.forEach(ch -> {
                    if (ch.isActive() && ch != exclude) {
                        ch.writeAndFlush(json + "\n");
                    }
                });
            }

        } catch (Exception e) {
            log.error("Ошибка при рассылке события {}", eventType, e);
        }
    }
}