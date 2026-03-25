package ru.alenavir.playerservice.mapper;

import org.mapstruct.Mapper;
import ru.alenavir.playerservice.dto.CreatePlayerDto;
import ru.alenavir.playerservice.dto.PlayerInfoDto;
import ru.alenavir.playerservice.entity.Player;
import ru.alenavir.playerservice.grpc.PlayerServiceProto;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    Player toEntity(PlayerInfoDto dto);
    PlayerInfoDto toDto(Player entity);

    Player toEntity(CreatePlayerDto dto);

    default PlayerServiceProto.PlayerInfo toProto(PlayerInfoDto dto) {
        return PlayerServiceProto.PlayerInfo.newBuilder()
                .setId(dto.getId())
                .setName(dto.getName())
                .setCurrentGameId(dto.getCurrentGameId() != null ? dto.getCurrentGameId() : 0L)
                .build();
    }

}
