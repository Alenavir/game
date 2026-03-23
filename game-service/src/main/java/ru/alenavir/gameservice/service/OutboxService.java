package ru.alenavir.gameservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alenavir.gameservice.dto.events.AggregateType;
import ru.alenavir.gameservice.dto.events.game.enums.GameEventType;
import ru.alenavir.gameservice.dto.events.player.enums.PlayerEventType;
import ru.alenavir.gameservice.entity.OutboxEvent;
import ru.alenavir.gameservice.entity.enums.EventType;
import ru.alenavir.gameservice.repo.OutboxRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepo outboxRepo;
    private final ObjectMapper objectMapper;

    public void saveEvent(AggregateType aggregateType,
                          String aggregateId,
                          EventType eventType,
                          Object eventDto) {

        try {
            String payload = objectMapper.writeValueAsString(eventDto);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateType(aggregateType.name());
            event.setAggregateId(aggregateId);
            event.setEventType(eventType.name());
            event.setPayload(payload);

            outboxRepo.save(event);

            log.info("Outbox event saved: type={}, aggregateId={}",
                    eventType, aggregateId);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации события", e);
        }
    }

    public void savePlayerEvent(Long playerId, PlayerEventType type, Object dto) {
        saveEvent(AggregateType.PLAYER, playerId.toString(), type, dto);
    }

    public void saveGameEvent(Long gameId, GameEventType type, Object dto) {
        saveEvent(AggregateType.GAME, gameId.toString(), type, dto);
    }
}
