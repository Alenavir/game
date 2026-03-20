package ru.alenavir.gameservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.gameservice.dto.GameInfoDto;
import ru.alenavir.gameservice.dto.events.PlayerJoinedEvent;
import ru.alenavir.gameservice.dto.events.PlayerLeftEvent;
import ru.alenavir.gameservice.entity.Game;
import ru.alenavir.gameservice.entity.OutboxEvent;
import ru.alenavir.gameservice.entity.enums.EventType;
import ru.alenavir.gameservice.entity.enums.GameState;
import ru.alenavir.gameservice.exceptions.BadRequestException;
import ru.alenavir.gameservice.exceptions.NotFoundException;
import ru.alenavir.gameservice.grpc.PlayerClient;
import ru.alenavir.gameservice.mapper.GameMapper;
import ru.alenavir.gameservice.repo.GameRepo;
import ru.alenavir.gameservice.repo.OutboxRepo;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepo gameRepo;
    private final OutboxRepo outboxRepo;
    private final PlayerClient playerClient;
    private final ObjectMapper objectMapper;
    private final GameMapper mapper;

    public Long createGame(Long playerId) {
        validatePlayer(playerId);

        Game game = new Game();
        game.setState(GameState.WAITING);
        game.setPlayerIds(new ArrayList<>(List.of(playerId)));

        Game saved = gameRepo.save(game);

        PlayerJoinedEvent eventDto = new PlayerJoinedEvent(playerId, saved.getId());

        String payload;
        try {
            payload = objectMapper.writeValueAsString(eventDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("PLAYER");
        event.setAggregateId(playerId.toString());
        event.setEventType(EventType.PLAYER_JOINED_GAME);
        event.setPayload(payload);

        outboxRepo.save(event);

        log.info("Игра {} создана с игроком {}", saved.getId(), playerId);

        return saved.getId();
    }

    @Transactional
    public void leaveGame(Long playerId, Long gameId) {
        Game game = gameRepo.findById(gameId).orElseThrow(() -> {
            log.warn("Игра с id={} не найдена", gameId);
            return new NotFoundException("Game with id " + gameId + " not found");
        });

        if (!game.getPlayerIds().contains(playerId)) {
            log.warn("Игрок id={} не участвует в игре id={}", playerId, gameId);
            throw new BadRequestException("Player is not in this game");
        }

        game.getPlayerIds().remove(playerId);
        gameRepo.save(game);

        PlayerLeftEvent eventDto = new PlayerLeftEvent(playerId);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(eventDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("PLAYER");
        event.setAggregateId(playerId.toString());
        event.setEventType(EventType.PLAYER_LEFT_GAME);
        event.setPayload(payload);

        outboxRepo.save(event);

        log.info("Игрок id={} вышел из игры id={}", playerId, gameId);

        if (game.getPlayerIds().isEmpty()) {
            game.setState(GameState.FINISHED);
            gameRepo.save(game);
            log.info("Игра id={} завершена, так как игроков не осталось", gameId);
        }

    }

    @Transactional
    public void joinGame(Long playerId, Long gameId) {
        validatePlayer(playerId);

        Game game = gameRepo.findById(gameId).orElseThrow(() -> {
            log.warn("Игра с id={} не найдена", gameId);
            return new NotFoundException("Game with id " + gameId + " not found");
        });

        if (game.getState() != GameState.WAITING) {
            throw new BadRequestException("Game already started");
        }

        if (game.getPlayerIds().contains(playerId)) {
            throw new BadRequestException("Player already in game");
        }

        game.getPlayerIds().add(playerId);
        gameRepo.save(game);

        PlayerJoinedEvent eventDto = new PlayerJoinedEvent(playerId, gameId);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(eventDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("PLAYER");
        event.setAggregateId(playerId.toString());
        event.setEventType(EventType.PLAYER_JOINED_GAME);
        event.setPayload(payload);

        outboxRepo.save(event);

        log.info("Игрок {} присоединился к игре {}", playerId, gameId);
    }

    @Transactional
    public void startGame(Long gameId) {
        Game game = gameRepo.findById(gameId).orElseThrow( () -> {
            log.warn("Игра с id={} не найдена", gameId);
            return new NotFoundException("Game with id " + gameId + " not found");
        });

        if (game.getPlayerIds().size() < 2) {
            throw new BadRequestException("Not enough players");
        }

        game.setState(GameState.RUNNING);

        gameRepo.save(game);

        log.info("Игра {} началась", gameId);
    }

    public GameInfoDto getGameInfo(Long gameId) {
        Game found = gameRepo.findByIdWithPlayers(gameId).orElseThrow( () -> {
            log.warn("Игра с id={} не найдена", gameId);
            return new NotFoundException("Game with id " + gameId + " not found");
        });

        return mapper.toDto(found);
    }

    private void validatePlayer(Long playerId) {
        try {
            playerClient.hasPlayer(playerId);
        } catch (Exception e) {
            throw new NotFoundException("Player not found: " + playerId);
        }
    }
}
