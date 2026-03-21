package ru.alenavir.unitservice.factory;

import ru.alenavir.unitservice.entity.Position;
import ru.alenavir.unitservice.entity.Unit;
import ru.alenavir.unitservice.entity.enums.UnitType;

public class UnitFactory {

    public static Unit createUnit(UnitType type, Position position, Long ownerId, Long gameId) {
        Unit unit = new Unit();
        unit.setType(type);
        unit.setPosition(position);
        unit.setOwnerId(ownerId);
        unit.setGameId(gameId);

        switch (type) {
            case MAGICIAN -> unit.setHealth(75);
            case KNIGHT -> unit.setHealth(100);
            default -> unit.setHealth(50);
        }

        return unit;
    }
}
