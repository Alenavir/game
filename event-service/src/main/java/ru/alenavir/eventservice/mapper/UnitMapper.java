package ru.alenavir.eventservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import ru.alenavir.eventservice.dto.AttackUnitDto;
import ru.alenavir.eventservice.dto.UnitDto;
import ru.alenavir.unitservice.grpc.UnitServiceProto;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    @ValueMapping(source = "UNRECOGNIZED", target = "UNKNOWN")
    UnitDto toDto(UnitServiceProto.UnitInfo unit);
    AttackUnitDto toDto(UnitServiceProto.AttackUnitResponse unit);

}
