package ru.alenavir.gameservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.alenavir.gameservice.dto.GameInfoDto;
import ru.alenavir.gameservice.service.GameService;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GameGrpcService extends GameServiceGrpc.GameServiceImplBase {

    private final GameService gameService;

    @Override
    public void createGame(GameServiceProto.CreateGameRequest request,
                           StreamObserver<GameServiceProto.CreateGameResponse> responseObserver) {
        GameInfoDto gameInfo = gameService.createGame(request.getPlayerId());

        GameServiceProto.GameInfo protoGameInfo = mapToProto(gameInfo);

        GameServiceProto.CreateGameResponse response = GameServiceProto.CreateGameResponse.newBuilder()
                .setGame(protoGameInfo)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void joinGame(GameServiceProto.JoinGameRequest request,
                         StreamObserver<GameServiceProto.JoinGameResponse> responseObserver) {
        GameInfoDto gameInfo = gameService.joinGame(request.getPlayerId(), request.getGameId());

        GameServiceProto.JoinGameResponse response = GameServiceProto.JoinGameResponse.newBuilder()
                .setGame(mapToProto(gameInfo))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void leaveGame(GameServiceProto.LeaveGameRequest request,
                          StreamObserver<GameServiceProto.LeaveGameResponse> responseObserver) {
        GameInfoDto gameInfo = gameService.leaveGame(request.getPlayerId(), request.getGameId());

        GameServiceProto.LeaveGameResponse response = GameServiceProto.LeaveGameResponse.newBuilder()
                .setGame(mapToProto(gameInfo))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void startGame(GameServiceProto.StartGameRequest request,
                          StreamObserver<GameServiceProto.StartGameResponse> responseObserver) {
        GameInfoDto gameInfo = gameService.startGame(request.getGameId());

        GameServiceProto.StartGameResponse response = GameServiceProto.StartGameResponse.newBuilder()
                .setGame(mapToProto(gameInfo))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getGame(GameServiceProto.GetGameRequest request,
                        StreamObserver<GameServiceProto.GetGameResponse> responseObserver) {
        GameInfoDto gameInfo = gameService.getGameInfo(request.getGameId());

        GameServiceProto.GetGameResponse response = GameServiceProto.GetGameResponse.newBuilder()
                .setGame(mapToProto(gameInfo))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void isGameRunning(GameServiceProto.IsGameRunningRequest request,
                              StreamObserver<GameServiceProto.IsGameRunningResponse> responseObserver) {
        boolean running = gameService.isGameRunning(request.getGameId());

        GameServiceProto.IsGameRunningResponse response = GameServiceProto.IsGameRunningResponse.newBuilder()
                .setExists(running)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // --- Вспомогательный метод для маппинга GameInfoDto в proto ---
    private GameServiceProto.GameInfo mapToProto(GameInfoDto dto) {
        GameServiceProto.GameState state = GameServiceProto.GameState.valueOf(dto.getState().name());
        List<Long> playerIds = dto.getPlayerIds();

        return GameServiceProto.GameInfo.newBuilder()
                .setId(dto.getId())
                .setState(state)
                .addAllPlayerIds(playerIds)
                .build();
    }
}
