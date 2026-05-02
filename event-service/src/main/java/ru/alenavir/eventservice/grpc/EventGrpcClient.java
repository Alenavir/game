package ru.alenavir.eventservice.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.eventservice.dto.AttackUnitDto;
import ru.alenavir.eventservice.dto.GameDto;
import ru.alenavir.eventservice.dto.PlayerDto;
import ru.alenavir.eventservice.dto.UnitDto;
import ru.alenavir.eventservice.mapper.GameMapper;
import ru.alenavir.eventservice.mapper.PlayerMapper;
import ru.alenavir.eventservice.mapper.UnitMapper;
import ru.alenavir.eventservice.service.netty.handler.unit.UnitCommandType;
import ru.alenavir.gameservice.grpc.GameServiceGrpc;
import ru.alenavir.gameservice.grpc.GameServiceProto;
import ru.alenavir.playerservice.grpc.PlayerServiceGrpc;
import ru.alenavir.playerservice.grpc.PlayerServiceProto;
import ru.alenavir.unitservice.grpc.UnitServiceGrpc;
import ru.alenavir.unitservice.grpc.UnitServiceProto;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventGrpcClient {

    private final PlayerServiceGrpc.PlayerServiceBlockingStub playerService;
    private final GameServiceGrpc.GameServiceBlockingStub gameService;
    private final UnitServiceGrpc.UnitServiceBlockingStub unitService;

    private final GameMapper gameMapper;
    private final PlayerMapper playerMapper;
    private final UnitMapper unitMapper;

    // PLAYER
    public PlayerDto createPlayer(String name) {
        PlayerServiceProto.CreatePlayerRequest grpcRequest =
                PlayerServiceProto.CreatePlayerRequest.newBuilder()
                        .setName(name)
                        .build();

        PlayerServiceProto.CreatePlayerResponse grpcResponse = playerService.createPlayer(grpcRequest);

        log.info("Игрок создан: id={}, имя={}", grpcResponse.getPlayer().getId(), grpcResponse.getPlayer().getName());
        return playerMapper.toDto(grpcResponse.getPlayer());
    }

    // GAME
    public GameDto createGame(Long playerId) {
        GameServiceProto.CreateGameRequest grpcRequest =
                GameServiceProto.CreateGameRequest.newBuilder()
                        .setPlayerId(playerId)
                        .build();

        GameServiceProto.CreateGameResponse grpcResponse = gameService.createGame(grpcRequest);

        log.info("Игра id={} создана игроком {}", grpcResponse.getGame().getId(), playerId);

        return gameMapper.toDto(grpcResponse.getGame());
    }

    public GameDto joinGame(Long playerId, Long gameId) {
        GameServiceProto.JoinGameRequest grpcRequest =
                GameServiceProto.JoinGameRequest.newBuilder()
                        .setPlayerId(playerId)
                        .setGameId(gameId)
                        .build();

        GameServiceProto.JoinGameResponse grpcResponse = gameService.joinGame(grpcRequest);
        log.info("Игрок id={} присоединился к игре id={}", playerId, grpcResponse.getGame().getId());

        return gameMapper.toDto(grpcResponse.getGame());
    }

    public GameDto leaveGame(Long playerId, Long gameId) {
        GameServiceProto.LeaveGameRequest grpcRequest =
                GameServiceProto.LeaveGameRequest.newBuilder()
                        .setPlayerId(playerId)
                        .setGameId(gameId)
                        .build();

        GameServiceProto.LeaveGameResponse grpcResponse = gameService.leaveGame(grpcRequest);
        log.info("Игрок id={} покинул игру id={}", playerId, grpcResponse.getGame().getId());

        return gameMapper.toDto(grpcResponse.getGame());
    }

    public GameDto startGame(Long gameId) {
        GameServiceProto.StartGameRequest grpcRequest =
                GameServiceProto.StartGameRequest.newBuilder()
                        .setGameId(gameId)
                        .build();

        GameServiceProto.StartGameResponse grpcResponse = gameService.startGame(grpcRequest);

        log.info("Игра id={} стартовала", grpcResponse.getGame().getId());

        return gameMapper.toDto(grpcResponse.getGame());
    }

    public GameDto getGame(Long gameId) {
        GameServiceProto.GetGameRequest grpcRequest =
                GameServiceProto.GetGameRequest.newBuilder()
                        .setGameId(gameId)
                        .build();

        GameServiceProto.GetGameResponse grpcResponse = gameService.getGame(grpcRequest);

        GameServiceProto.GameInfo game = grpcResponse.getGame();

        log.info("Получена игра: id={}, state={}, players={}",
                game.getId(),
                game.getState(),
                game.getPlayerIdsList());

        return gameMapper.toDto(grpcResponse.getGame());
    }

    // UNIT
    public UnitDto createUnit(UnitServiceProto.UnitType type, double x, double y, Long playerId, Long gameId) {
        UnitServiceProto.CreateUnitRequest grpcRequest =
                UnitServiceProto.CreateUnitRequest.newBuilder()
                        .setType(type)
                        .setX(x)
                        .setY(y)
                        .setPlayerId(playerId)
                        .setGameId(gameId)
                        .build();

        UnitServiceProto.CreateUnitResponse grpcResponse = unitService.createUnit(grpcRequest);

        log.info("Юнит создан: id={}, тип={}, позиция=({}, {})",
                grpcResponse.getUnit().getId(), grpcResponse.getUnit().getType(),
                grpcResponse.getUnit().getX(), grpcResponse.getUnit().getY());

        return unitMapper.toDto(grpcResponse.getUnit());
    }

    public UnitDto moveUnit(double x, double y, Long playerId, Long unitId) {
        UnitServiceProto.MoveUnitRequest grpcRequest =
                UnitServiceProto.MoveUnitRequest.newBuilder()
                        .setX(x)
                        .setY(y)
                        .setPlayerId(playerId)
                        .setUnitId(unitId)
                        .build();

        UnitServiceProto.MoveUnitResponse grpcResponse = unitService.moveUnit(grpcRequest);

        log.info("Юнит пермещён с id={} на позицию =({}, {}) в игре {}",
                grpcResponse.getUnit().getId(), grpcResponse.getUnit().getX(),
                grpcResponse.getUnit().getY(), grpcResponse.getUnit().getGameId());

        return unitMapper.toDto(grpcResponse.getUnit());
    }

    public AttackUnitDto attackUnit(Long playerId, Long targetId, Long attackerId) {
        UnitServiceProto.AttackUnitRequest grpcRequest =
                UnitServiceProto.AttackUnitRequest.newBuilder()
                        .setPlayerId(playerId)
                        .setTargetId(targetId)
                        .setAttackerId(attackerId)
                        .build();

        UnitServiceProto.AttackUnitResponse grpcResponse = unitService.attackUnit(grpcRequest);

        log.info("Атака игрока id = {}: attackerId = {}, targetId = {}, урон = {}, цель мертва = {}",
                playerId,
                grpcResponse.getAttacker().getId(),
                grpcResponse.getTarget().getId(),
                grpcResponse.getDamage(),
                grpcResponse.getTargetDead());

        return unitMapper.toDto(grpcResponse);
    }
}