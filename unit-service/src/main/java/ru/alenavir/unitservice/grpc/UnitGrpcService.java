package ru.alenavir.unitservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.alenavir.unitservice.dto.CreatedUnitDto;
import ru.alenavir.unitservice.dto.UnitInfoDto;
import ru.alenavir.unitservice.entity.enums.UnitType;
import ru.alenavir.unitservice.service.UnitService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UnitGrpcService extends UnitServiceGrpc.UnitServiceImplBase {

    private final UnitService unitService;

    // Конвертация enum
    private UnitServiceProto.UnitType toProtoUnitType(UnitType type) {
        if (type == null) return UnitServiceProto.UnitType.UNKNOWN;
        return switch (type) {
            case KNIGHT -> UnitServiceProto.UnitType.KNIGHT;
            case MAGICIAN -> UnitServiceProto.UnitType.MAGICIAN;
            default -> UnitServiceProto.UnitType.UNKNOWN;
        };
    }

    @Override
    public void createUnit(UnitServiceProto.CreateUnitRequest request,
                           StreamObserver<UnitServiceProto.CreateUnitResponse> responseObserver) {
        try {
            CreatedUnitDto dto = new CreatedUnitDto();
            dto.setType(UnitType.valueOf(request.getType().name()));
            dto.setX(request.getX());
            dto.setY(request.getY());
            dto.setOwnerId(request.getOwnerId());
            dto.setGameId(request.getGameId());

            UnitInfoDto unitInfo = unitService.createUnit(dto);

            UnitServiceProto.UnitInfo unitProto = UnitServiceProto.UnitInfo.newBuilder()
                    .setId(unitInfo.getId())
                    .setType(toProtoUnitType(unitInfo.getType()))
                    .setX(unitInfo.getX())
                    .setY(unitInfo.getY())
                    .setOwnerId(unitInfo.getOwnerId())
                    .setGameId(unitInfo.getGameId())
                    .setHealth(unitInfo.getHealth())
                    .build();

            UnitServiceProto.CreateUnitResponse response = UnitServiceProto.CreateUnitResponse.newBuilder()
                    .setUnit(unitProto)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void moveUnit(UnitServiceProto.MoveUnitRequest request,
                         StreamObserver<UnitServiceProto.MoveUnitResponse> responseObserver) {
        try {
            UnitInfoDto unitInfo = unitService.moveUnit(
                    request.getUnitId(),
                    request.getPlayerId(),
                    request.getX(),
                    request.getY()
            );

            UnitServiceProto.UnitInfo unitProto = UnitServiceProto.UnitInfo.newBuilder()
                    .setId(unitInfo.getId())
                    .setType(toProtoUnitType(unitInfo.getType()))
                    .setX(unitInfo.getX())
                    .setY(unitInfo.getY())
                    .setOwnerId(unitInfo.getOwnerId())
                    .setGameId(unitInfo.getGameId())
                    .setHealth(unitInfo.getHealth())
                    .build();

            UnitServiceProto.MoveUnitResponse response = UnitServiceProto.MoveUnitResponse.newBuilder()
                    .setUnit(unitProto)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}