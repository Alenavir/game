package ru.alenavir.eventservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import ru.alenavir.eventservice.dto.GameDto;
import ru.alenavir.gameservice.grpc.GameServiceProto;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @ValueMapping(source = "UNRECOGNIZED", target = "UNKNOWN")
    GameDto toDto(GameServiceProto.GameInfo game);

}
