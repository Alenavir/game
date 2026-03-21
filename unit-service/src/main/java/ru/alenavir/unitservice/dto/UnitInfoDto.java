package ru.alenavir.unitservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.unitservice.entity.enums.UnitType;

@Setter
@Getter
@AllArgsConstructor
public class UnitInfoDto {

    private Long id;

    private UnitType type;

    private double x;

    private double y;

    private Long ownerId;

    private Long gameId;

    private int health;

}
