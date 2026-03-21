package ru.alenavir.unitservice.dto.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.alenavir.unitservice.entity.enums.UnitType;

@Getter
@Setter
@NoArgsConstructor
public class UnitCreatedEvent implements UnitEvent {

    private String eventType = "UNIT_CREATED"; // фиксированный тип события
    private Long gameId;      // id матча
    private Long unitId;      // id юнита
    private Long ownerId;     // id игрока-владельца
    private UnitType type;      // "MAGE", "KNIGHT"
    private double x;         // координата X
    private double y;         // координата Y
    private int health;       // HP юнита

    public UnitCreatedEvent(Long gameId, Long unitId, Long ownerId, UnitType type, double x, double y, int health) {
        this.gameId = gameId;
        this.unitId = unitId;
        this.ownerId = ownerId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.health = health;
    }
}