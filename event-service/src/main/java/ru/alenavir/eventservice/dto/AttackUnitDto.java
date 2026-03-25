package ru.alenavir.eventservice.dto;

public record AttackUnitDto(
        UnitDto attacker,
        UnitDto target,   // может быть null
        int damage,
        boolean targetDead
) {}
