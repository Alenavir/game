package ru.alenavir.eventservice.service.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

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
    private final Map<Long, ScheduledFuture<?>> disconnectTasks = new ConcurrentHashMap<>();

    public void addChannel(String gameId, Channel channel) {
        gameChannels
                .computeIfAbsent(gameId, k -> new ConcurrentLinkedQueue<>())
                .add(channel);
    }

    public void removeChannel(String gameId, Channel channel) {
        gameChannels.computeIfPresent(gameId, (k, channels) -> {
            channels.remove(channel);
            return channels.isEmpty() ? null : channels;
        });
    }

    public void scheduleDisconnect(Long playerId, ScheduledFuture<?> task) {
        disconnectTasks.put(playerId, task);
    }

    public void cancelDisconnect(Long playerId) {
        ScheduledFuture<?> task = disconnectTasks.remove(playerId);
        if (task != null) {
            task.cancel(false);
            log.info("Таймер реконнекта для игрока {} отменён", playerId);
        }
    }

    public void broadcastToGame(String gameId, Object payload, String eventType, Channel exclude) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "eventType", eventType,
                    "payload", payload
            ));

            ConcurrentLinkedQueue<Channel> channels = gameChannels.get(gameId);

            if (channels == null || channels.isEmpty()) {
                log.warn("broadcastToGame: нет каналов для игры {}, eventType={}", gameId, eventType);
                return;
            }

            int sent = 0;
            for (Channel ch : channels) {
                if (ch.isActive() && ch != exclude) {
                    ch.writeAndFlush(json + "\n");
                    sent++;
                }
            }

            log.debug("broadcast gameId={} eventType={} отправлено={} из={}",
                    gameId, eventType, sent, channels.size());

            if (sent == 0) {
                log.warn("broadcast gameId={} eventType={} — никто не получил (каналов={})",
                        gameId, eventType, channels.size());
            }

        } catch (Exception e) {
            log.error("Ошибка при рассылке события {} для игры {}", eventType, gameId, e);
        }
    }
}