package ru.alenavir.gameservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CreateGameDto {
    private List<Long> playerIds;
}
