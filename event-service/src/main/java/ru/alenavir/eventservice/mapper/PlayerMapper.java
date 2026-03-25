package ru.alenavir.eventservice.mapper;

import org.mapstruct.Mapper;
import ru.alenavir.eventservice.dto.PlayerDto;
import ru.alenavir.playerservice.grpc.PlayerServiceProto;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerDto toDto(PlayerServiceProto.PlayerInfo player);

}
