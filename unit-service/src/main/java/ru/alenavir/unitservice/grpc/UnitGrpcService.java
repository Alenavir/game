package ru.alenavir.unitservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.alenavir.unitservice.dto.CreatedUnitDto;
import ru.alenavir.unitservice.dto.UnitInfoDto;
import ru.alenavir.unitservice.entity.Unit;
import ru.alenavir.unitservice.entity.enums.UnitType;
import ru.alenavir.unitservice.service.UnitService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UnitGrpcService extends UnitServiceGrpc.UnitServiceImplBase {

    private final UnitService unitService;

    // Конвертация enum
    private UnitServiceProto.UnitType toProtoUnitType(UnitType type) {
        if (type == null) return UnitServiceProto.UnitType.UNRECOGNIZED;
        return switch (type) {
            case KNIGHT -> UnitServiceProto.UnitType.KNIGHT;
            case MAGICIAN -> UnitServiceProto.UnitType.MAGICIAN;
            default -> UnitServiceProto.UnitType.UNRECOGNIZED;
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
            dto.setOwnerId(request.getPlayerId());
            dto.setGameId(request.getGameId());

            UnitInfoDto unitInfo = unitService.createUnit(dto);

            UnitServiceProto.UnitInfo unitProto = UnitServiceProto.UnitInfo.newBuilder()
                    .setId(unitInfo.getId())
                    .setType(toProtoUnitType(unitInfo.getType()))
                    .setX(unitInfo.getX())
                    .setY(unitInfo.getY())
                    .setPlayerId(unitInfo.getOwnerId())
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
                    .setPlayerId(unitInfo.getOwnerId())
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

    @Override
    public void attackUnit(UnitServiceProto.AttackUnitRequest request,
                           StreamObserver<UnitServiceProto.AttackUnitResponse> responseObserver) {
        try {
            UnitInfoDto attackerDto = unitService.attackUnit(
                    request.getAttackerId(),
                    request.getTargetId(),
                    request.getPlayerId()
            );

            // Цель из репозитория, если она ещё жива
            Unit target = unitService.getUnitById(request.getTargetId());
            UnitServiceProto.UnitInfo.Builder targetProtoBuilder = UnitServiceProto.UnitInfo.newBuilder();
            boolean targetDead = false;
            if (target != null) {
                targetProtoBuilder
                        .setId(target.getId())
                        .setType(toProtoUnitType(target.getType()))
                        .setX(target.getPosition().getX())
                        .setY(target.getPosition().getY())
                        .setPlayerId(target.getOwnerId())
                        .setGameId(target.getGameId())
                        .setHealth(target.getHealth());
            } else {
                targetDead = true;
            }

            // Конвертация атакующего
            UnitServiceProto.UnitInfo attackerProto = UnitServiceProto.UnitInfo.newBuilder()
                    .setId(attackerDto.getId())
                    .setType(toProtoUnitType(attackerDto.getType()))
                    .setX(attackerDto.getX())
                    .setY(attackerDto.getY())
                    .setPlayerId(attackerDto.getOwnerId())
                    .setGameId(attackerDto.getGameId())
                    .setHealth(attackerDto.getHealth())
                    .build();

            UnitServiceProto.AttackUnitResponse.Builder responseBuilder =
                    UnitServiceProto.AttackUnitResponse.newBuilder()
                            .setAttacker(attackerProto)
                            .setTarget(targetProtoBuilder)
                            .setDamage(10)
                            .setTargetDead(targetDead);

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}