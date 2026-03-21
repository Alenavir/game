package ru.alenavir.unitservice.dto;

import lombok.Getter;
import lombok.Setter;
import ru.alenavir.unitservice.entity.enums.UnitType;

@Setter
@Getter
public class CreatedUnitDto {

    private Long id;

    private UnitType type;

    private double x;

    private double y;

    private Long ownerId;

    private Long gameId;

}
