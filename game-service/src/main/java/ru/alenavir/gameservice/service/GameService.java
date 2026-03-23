package ru.alenavir.gameservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.gameservice.dto.GameInfoDto;
import ru.alenavir.gameservice.dto.events.game.GameFinishedEvent;
import ru.alenavir.gameservice.dto.events.game.GameStartedEvent;
import ru.alenavir.gameservice.dto.events.game.enums.GameEventType;
import ru.alenavir.gameservice.dto.events.player.PlayerJoinedEvent;
import ru.alenavir.gameservice.dto.events.player.PlayerLeftEvent;
import ru.alenavir.gameservice.dto.events.player.enums.PlayerEventType;
import ru.alenavir.gameservice.entity.Game;
import ru.alenavir.gameservice.entity.enums.GameState;
import ru.alenavir.gameservice.exceptions.BadRequestException;
import ru.alenavir.gameservice.exceptions.NotFoundException;
import ru.alenavir.gameservice.grpc.PlayerClient;
import ru.alenavir.gameservice.mapper.GameMapper;
import ru.alenavir.gameservice.repo.GameRepo;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepo gameRepo;
    private final PlayerClient playerClient;
    private final GameMapper mapper;
    private final OutboxService outboxService;

    public Long createGame(Long playerId) {
        validatePlayer(playerId);

        Game game = new Game();
        game.setState(GameState.WAITING);
        game.setPlayerIds(new ArrayList<>(List.of(playerId)));

        Game saved = gameRepo.save(game);

        outboxService.savePlayerEvent(
                playerId,
                PlayerEventType.PLAYER_JOINED_GAME,
                new PlayerJoinedEvent(playerId, saved.getId())
        );

        log.info("Игра {} создана с игроком {}", saved.getId(), playerId);

        return saved.getId();
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

        outboxService.savePlayerEvent(
                playerId,
                PlayerEventType.PLAYER_JOINED_GAME,
                new PlayerJoinedEvent(playerId, gameId)
        );

        log.info("Игрок {} присоединился к игре {}", playerId, gameId);
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

        outboxService.savePlayerEvent(
                playerId,
                PlayerEventType.PLAYER_LEFT_GAME,
                new PlayerLeftEvent(playerId, gameId)
        );

        log.info("Игрок id={} вышел из игры id={}", playerId, gameId);

        finishGameIfEmpty(game);
    }

    @Transactional
    public void startGame(Long gameId) {
        Game game = gameRepo.findById(gameId).orElseThrow(() -> {
            log.warn("Игра с id={} не найдена", gameId);
            return new NotFoundException("Game with id " + gameId + " not found");
        });

        if (game.getPlayerIds().size() < 2) {
            throw new BadRequestException("Not enough players");
        }

        game.setState(GameState.RUNNING);
        gameRepo.save(game);

        outboxService.saveGameEvent(
                gameId,
                GameEventType.GAME_STARTED,
                new GameStartedEvent(gameId, game.getPlayerIds())
        );

        log.info("Игра {} началась", gameId);
    }

    private void finishGameIfEmpty(Game game) {
        if (!game.getPlayerIds().isEmpty()) {
            return;
        }

        game.setState(GameState.FINISHED);
        gameRepo.save(game);

        outboxService.saveGameEvent(
                game.getId(),
                GameEventType.GAME_FINISHED,
                new GameFinishedEvent(game.getId())
        );

        log.info("Игра id={} завершена, так как игроков не осталось", game.getId());
    }

    public GameInfoDto getGameInfo(Long gameId) {
        Game game = gameRepo.findByIdWithPlayers(gameId).orElseThrow(() -> {
            log.warn("Игра с id={} не найдена", gameId);
            return new NotFoundException("Game with id " + gameId + " not found");
        });

        return mapper.toDto(game);
    }

    public boolean isGameRunning(Long gameId) {
        return gameRepo.existsByIdAndState(gameId, GameState.RUNNING);
    }

    private void validatePlayer(Long playerId) {
        try {
            playerClient.hasPlayer(playerId);
        } catch (Exception e) {
            throw new NotFoundException("Player not found: " + playerId);
        }
    }
}