package ru.alenavir.gameservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.gameservice.dto.GameInfoDto;
import ru.alenavir.gameservice.entity.Game;
import ru.alenavir.gameservice.entity.enums.GameState;
import ru.alenavir.gameservice.exceptions.BadRequestException;
import ru.alenavir.gameservice.exceptions.NotFoundException;
import ru.alenavir.gameservice.grpc.PlayerClient;
import ru.alenavir.gameservice.repo.GameRepo;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepo gameRepo;
    private final PlayerClient playerClient;

    public Long createGame(Long playerId) {
        validatePlayer(playerId);

        Game game = new Game();
        game.setState(GameState.WAITING);
        game.setPlayerIds(new ArrayList<>(List.of(playerId)));

        Game saved = gameRepo.save(game);

        log.info("Игра {} создана с игроком {}", saved.getId(), playerId);

        return saved.getId();
    }

    @Transactional
    public void joinGame(Long playerId, Long gameId) {
        validatePlayer(playerId);

        Game game = gameRepo.findById(gameId).orElseThrow( () -> {
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

    //TODO ЗАЧЕМ СУЩНОСТЬ ВОЗВРАЩАТЬ
    private void validatePlayer(Long playerId) {
        try {
            playerClient.getPlayer(playerId);
        } catch (Exception e) {
            throw new NotFoundException("Player not found: " + playerId);
        }
    }
}
