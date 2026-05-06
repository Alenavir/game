package ru.alenavir.eventservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import ru.alenavir.eventservice.dto.GameDto;
import ru.alenavir.gameservice.grpc.GameServiceProto;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(target = "playerIds", expression = "java(game.getPlayerIdsList())")
    @ValueMapping(source = "UNRECOGNIZED", target = "UNKNOWN")
    GameDto toDto(GameServiceProto.GameInfo game);
}