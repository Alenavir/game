package ru.alenavir.gameservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.gameservice.dto.GameInfoDto;
import ru.alenavir.gameservice.service.GameService;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameGrpcService extends GameServiceGrpc.GameServiceImplBase {

    private final GameService gameService;

    @Override
    public void createGame(GameServiceProto.CreateGameRequest request,
                           StreamObserver<GameServiceProto.CreateGameResponse> responseObserver) {

        Long gameId = gameService.createGame(request.getPlayerId());

        responseObserver.onNext(
                GameServiceProto.CreateGameResponse.newBuilder()
                        .setGameId(gameId)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void joinGame(GameServiceProto.JoinGameRequest request,
                         StreamObserver<GameServiceProto.JoinGameResponse> responseObserver) {

        gameService.joinGame(request.getPlayerId(), request.getGameId());

        responseObserver.onNext(GameServiceProto.JoinGameResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void leaveGame(GameServiceProto.LeaveGameRequest request,
                          StreamObserver<GameServiceProto.LeaveGameResponse> responseObserver) {

        gameService.leaveGame(request.getPlayerId(), request.getGameId());

        responseObserver.onNext(GameServiceProto.LeaveGameResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void startGame(GameServiceProto.StartGameRequest request,
                          StreamObserver<GameServiceProto.StartGameResponse> responseObserver) {

        gameService.startGame(request.getGameId());

        responseObserver.onNext(GameServiceProto.StartGameResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGame(GameServiceProto.GetGameRequest request,
                        StreamObserver<GameServiceProto.GetGameResponse> responseObserver) {

        GameInfoDto gameInfo = gameService.getGameInfo(request.getGameId());

        GameServiceProto.GameInfo protoGameInfo = GameServiceProto.GameInfo.newBuilder()
                .setId(gameInfo.getId())
                .setState(GameServiceProto.GameState.valueOf(gameInfo.getState().name()))
                .addAllPlayerIds(gameInfo.getPlayerIds())
                .build();

        responseObserver.onNext(
                GameServiceProto.GetGameResponse.newBuilder()
                        .setGame(protoGameInfo)
                        .build()
        );
        responseObserver.onCompleted();
    }
}
