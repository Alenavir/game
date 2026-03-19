package ru.alenavir.gameservice.dto;

import lombok.Getter;
import lombok.Setter;
import ru.alenavir.gameservice.entity.enums.GameState;

import java.util.List;

@Getter
@Setter
public class GameInfoDto {

    private Long id;

    private List<Long> playerIds;

    private GameState state;
}
