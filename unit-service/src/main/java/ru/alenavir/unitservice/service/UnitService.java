package ru.alenavir.unitservice.service;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alenavir.unitservice.dto.CreatedUnitDto;
import ru.alenavir.unitservice.dto.UnitInfoDto;
import ru.alenavir.unitservice.entity.Position;
import ru.alenavir.unitservice.entity.Unit;
import ru.alenavir.unitservice.exceptions.BadRequestException;
import ru.alenavir.unitservice.exceptions.NotFoundException;
import ru.alenavir.unitservice.factory.UnitFactory;
import ru.alenavir.unitservice.grpc.client.GameClient;
import ru.alenavir.unitservice.grpc.client.PlayerClient;
import ru.alenavir.unitservice.mapper.UnitMapper;
import ru.alenavir.unitservice.repo.UnitRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitService {

    private final UnitRepo repo;
    private final PlayerClient playerClient;
    private final GameClient gameClient;
    private final UnitMapper mapper;

    public UnitInfoDto createUnit(CreatedUnitDto unitDto) {
        validatePlayer(unitDto.getOwnerId());
        validateGame(unitDto.getGameId());

        Position position = mapper.toPosition(unitDto.getX(), unitDto.getY());
        validatePosition(position);

        Unit unit = UnitFactory.createUnit(
                unitDto.getType(),
                position,
                unitDto.getOwnerId(),
                unitDto.getGameId()
        );

        Unit savedUnit = repo.save(unit);

        log.info("Создан юнит {} типа {} для игрока {} в игре {}",
                savedUnit.getId(),
                savedUnit.getType(),
                savedUnit.getOwnerId(),
                savedUnit.getGameId());

        return mapper.toDto(savedUnit);
    }

    @Transactional
    public UnitInfoDto moveUnit(Long unitId, Long playerId, double x, double y) {
        validatePlayer(playerId);

        Unit unit = repo.findById(unitId).orElseThrow(() -> {
            log.warn("Юнит с id={} не найден", unitId);
            return new NotFoundException("Unit with id " + unitId + " not found");
        });

        validateGame(unit.getGameId());

        if (!unit.getOwnerId().equals(playerId)) {
            log.warn("Игрок {} пытается двигать чужой юнит {}", playerId, unitId);
            throw new BadRequestException("Unit does not belong to player");
        }

        Position prev = unit.getPosition();

        Position newPosition = mapper.toPosition(x, y);
        validatePosition(newPosition);

        unit.setPosition(newPosition);

        log.info("Юнит {} перемещён игроком {} с ({}, {}) на ({}, {})",
                unit.getId(),
                playerId,
                prev.getX(), prev.getY(),
                newPosition.getX(), newPosition.getY());

        return mapper.toDto(unit);
    }

    private void validatePlayer(Long playerId) {
        try {
            if (!playerClient.hasCurrentGame(playerId)) {
                log.warn("Проверка игрока {} не пройдена: игрок не в матче", playerId);
                throw new BadRequestException("Player is not in a game: " + playerId);
            }
        } catch (StatusRuntimeException e) {
            log.error("Ошибка при проверке игрока {}: {}", playerId, e.getMessage());
            throw new BadRequestException("Failed to validate player: " + playerId);
        }
    }

    private void validateGame(Long gameId) {
        try {
            if (!gameClient.isGameRunning(gameId)) {
                log.warn("Проверка игры {} не пройдена: игра не запущена", gameId);
                throw new BadRequestException("Game is not running: " + gameId);
            }
        } catch (StatusRuntimeException e) {
            log.error("Ошибка при проверке игры {}: {}", gameId, e.getMessage());
            throw new BadRequestException("Failed to validate game: " + gameId);
        }
    }

    private void validatePosition(Position position) {
        if (position.getX() < 0 || position.getY() < 0) {
            log.warn("Некорректная позиция юнита: x={}, y={}", position.getX(), position.getY());
            throw new BadRequestException("Invalid unit position: " + position);
        }
    }
}
