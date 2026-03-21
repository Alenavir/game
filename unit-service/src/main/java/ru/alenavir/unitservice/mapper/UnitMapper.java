package ru.alenavir.unitservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.alenavir.unitservice.dto.CreatedUnitDto;
import ru.alenavir.unitservice.dto.UnitInfoDto;
import ru.alenavir.unitservice.entity.Position;
import ru.alenavir.unitservice.entity.Unit;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    @Mapping(target = "position", expression = "java(toPosition(dto.getX(), dto.getY()))")
    Unit toEntity(CreatedUnitDto dto);

    @Mapping(target = "x", source = "position.x")
    @Mapping(target = "y", source = "position.y")
    UnitInfoDto toDto(Unit unit);

    default Position toPosition(double x, double y) {
        Position pos = new Position();
        pos.setX(x);
        pos.setY(y);
        return pos;
    }
}