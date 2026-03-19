package ru.alenavir.gameservice.mapper;

import org.mapstruct.Mapper;
import ru.alenavir.gameservice.dto.GameInfoDto;
import ru.alenavir.gameservice.entity.Game;

@Mapper(componentModel = "spring")
public interface GameMapper {

    GameInfoDto toDto(Game game);

    Game toEntity(GameInfoDto dto);

}
