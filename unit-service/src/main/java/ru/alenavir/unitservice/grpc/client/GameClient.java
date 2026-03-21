package ru.alenavir.unitservice.grpc.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.alenavir.gameservice.grpc.GameServiceGrpc;
import ru.alenavir.gameservice.grpc.GameServiceProto;

@Component
@RequiredArgsConstructor
public class GameClient {

    private final GameServiceGrpc.GameServiceBlockingStub stub;

    public boolean isGameRunning(Long gameId) {
        return stub.isGameRunning(
                GameServiceProto.IsGameRunningRequest.newBuilder()
                        .setGameId(gameId)
                        .build()
        ).getExists();
    }
}
