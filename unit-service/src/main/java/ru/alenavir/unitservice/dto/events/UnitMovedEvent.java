package ru.alenavir.unitservice.dto.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnitMovedEvent implements UnitEvent {

    private String eventType = "UNIT_MOVED"; // фиксированный тип события
    private Long gameId;      // id матча
    private Long unitId;      // id юнита
    private double x;         // новая координата X
    private double y;         // новая координата Y

    public UnitMovedEvent(Long gameId, Long unitId, double x, double y) {
        this.gameId = gameId;
        this.unitId = unitId;
        this.x = x;
        this.y = y;
    }
}