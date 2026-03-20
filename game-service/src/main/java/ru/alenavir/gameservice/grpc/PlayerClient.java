package ru.alenavir.gameservice.grpc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.alenavir.playerservice.grpc.PlayerServiceGrpc;
import ru.alenavir.playerservice.grpc.PlayerServiceProto;

@Component
@RequiredArgsConstructor
public class PlayerClient {

    private final PlayerServiceGrpc.PlayerServiceBlockingStub stub;

    public boolean hasPlayer(Long playerId) {
        return stub.hasPlayer(
                PlayerServiceProto.HasPlayerRequest.newBuilder()
                        .setPlayerId(playerId)
                        .build()
        ).getExists();
    }
}
