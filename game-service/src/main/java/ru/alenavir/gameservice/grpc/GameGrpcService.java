package ru.alenavir.gameservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alenavir.gameservice.service.GameService;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameGrpcService extends GameServiceGrpc.GameServiceImplBase {

    private final GameService gameService;

    @Override
    public void createGame(GameProto.CreateGameRequest request,
                           StreamObserver<GameProto.CreateGameResponse> responseObserver) {

        Long gameId = gameService.createGame(request.getPlayerId());

        responseObserver.onNext(
                GameProto.CreateGameResponse.newBuilder()
                        .setGameId(gameId)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void joinGame(GameProto.JoinGameRequest request,
                         StreamObserver<GameProto.JoinGameResponse> responseObserver) {

        gameService.joinGame(request.getPlayerId(), request.getGameId());

        responseObserver.onNext(GameProto.JoinGameResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void startGame(GameProto.StartGameRequest request,
                          StreamObserver<GameProto.StartGameResponse> responseObserver) {

        gameService.startGame(request.getGameId());

        responseObserver.onNext(GameProto.StartGameResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
