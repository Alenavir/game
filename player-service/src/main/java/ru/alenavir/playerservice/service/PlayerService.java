package ru.alenavir.playerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.playerservice.dto.CreatePlayerDto;
import ru.alenavir.playerservice.dto.PlayerInfoDto;
import ru.alenavir.playerservice.entity.Player;
import ru.alenavir.playerservice.exceptions.BadRequestException;
import ru.alenavir.playerservice.exceptions.NotFoundException;
import ru.alenavir.playerservice.mapper.PlayerMapper;
import ru.alenavir.playerservice.repo.PlayerRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepo repo;
    private final PlayerMapper mapper;

    public PlayerInfoDto getPlayerInfo(Long playerId) {
        Player found = repo.findById(playerId).orElseThrow(() -> {
            log.warn("Игрок с id={} не найден", playerId);
            return new NotFoundException("Player with id " + playerId + " not found");
        });

        log.info("Информация о игроке с id={} получена", playerId);
        return mapper.toDto(found);
    }

    public boolean hasPlayer(Long playerId) {
        boolean exists = repo.existsById(playerId);
        log.info("Проверка существования игрока с id={} → {}", playerId, exists);
        return exists;
    }

    public boolean hasCurrentGame(Long playerId) {
        boolean exists = repo.existsByIdAndCurrentGameIdIsNotNull(playerId);
        log.info("Проверка, есть ли у игрока {} текущая игра → {}", playerId, exists);
        return exists;
    }

    public PlayerInfoDto createPlayer(CreatePlayerDto createPlayerDto) {
        Player player = mapper.toEntity(createPlayerDto);
        player = repo.save(player);
        PlayerInfoDto dto = mapper.toDto(player);
        log.info("Создан игрок с id={} и именем={}", dto.getId(), dto.getName());
        return dto;
    }

    @Transactional
    public void assignToGame(Long playerId, Long gameId) {
        Player player = repo.findById(playerId).orElseThrow(() -> {
            log.warn("Игрок с id={} не найден", playerId);
            return new NotFoundException("Player with id " + playerId + " not found");
        });

        if (player.getCurrentGameId() != null) {
            throw new BadRequestException("Player is already in the game");
        }

        player.setCurrentGameId(gameId);
        repo.save(player);
        log.info("Игрок {} присоединился к игре {}", playerId, gameId);
    }

    @Transactional
    public void removeFromGame(Long playerId) {
        Player player = repo.findById(playerId).orElseThrow(() -> {
            log.warn("Игрок с id={} не найден", playerId);
            return new NotFoundException("Player with id " + playerId + " not found");
        });

        Long currentGameId = player.getCurrentGameId();

        if (currentGameId == null) {
            throw new BadRequestException("Player isn't in the game");
        }

        player.setCurrentGameId(null);
        repo.save(player);
        log.info("Игрок {} вышел из игры {}", playerId, currentGameId);
    }
}
