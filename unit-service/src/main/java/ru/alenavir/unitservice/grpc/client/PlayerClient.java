package ru.alenavir.unitservice.grpc.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.alenavir.playerservice.grpc.PlayerServiceGrpc;
import ru.alenavir.playerservice.grpc.PlayerServiceProto;

@Component
@RequiredArgsConstructor
public class PlayerClient {

    private final PlayerServiceGrpc.PlayerServiceBlockingStub stub;

    public boolean hasCurrentGame(Long playerId) {
        return stub.hasCurrentGame(
                PlayerServiceProto.HasCurrentGameRequest.newBuilder()
                        .setPlayerId(playerId)
                        .build()
        ).getExists();
    }
}