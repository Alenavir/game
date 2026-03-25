package ru.alenavir.playerservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.alenavir.playerservice.dto.CreatePlayerDto;
import ru.alenavir.playerservice.dto.PlayerInfoDto;
import ru.alenavir.playerservice.mapper.PlayerMapper;
import ru.alenavir.playerservice.service.PlayerService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class PlayerGrpcService extends PlayerServiceGrpc.PlayerServiceImplBase {

    private final PlayerService playerService;
    private final PlayerMapper mapper;

    @Override
    public void createPlayer(PlayerServiceProto.CreatePlayerRequest request,
                             StreamObserver<PlayerServiceProto.CreatePlayerResponse> responseObserver) {

        CreatePlayerDto createPlayerDto = new CreatePlayerDto();
        createPlayerDto.setName(request.getName());

        PlayerInfoDto dto = playerService.createPlayer(createPlayerDto);

        PlayerServiceProto.CreatePlayerResponse response = PlayerServiceProto.CreatePlayerResponse.newBuilder()
                .setPlayer(mapper.toProto(dto))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPlayerInfo(PlayerServiceProto.GetPlayerInfoRequest request,
                              StreamObserver<PlayerServiceProto.GetPlayerInfoResponse> responseObserver) {

        PlayerInfoDto dto = playerService.getPlayerInfo(request.getPlayerId());

        PlayerServiceProto.GetPlayerInfoResponse response = PlayerServiceProto.GetPlayerInfoResponse.newBuilder()
                .setPlayer(mapper.toProto(dto))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void hasPlayer(PlayerServiceProto.HasPlayerRequest request,
                          StreamObserver<PlayerServiceProto.HasPlayerResponse> responseObserver) {

        boolean exists = playerService.hasPlayer(request.getPlayerId());

        PlayerServiceProto.HasPlayerResponse response = PlayerServiceProto.HasPlayerResponse.newBuilder()
                .setExists(exists)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void hasCurrentGame(PlayerServiceProto.HasCurrentGameRequest request,
                               StreamObserver<PlayerServiceProto.HasCurrentGameResponse> responseObserver) {

        boolean exists = playerService.hasCurrentGame(request.getPlayerId());
        long playerId = request.getPlayerId();

        PlayerServiceProto.HasCurrentGameResponse response = PlayerServiceProto.HasCurrentGameResponse.newBuilder()
                .setExists(exists)
                .setGameId(playerId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}