package ru.alenavir.playerservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerInfoDto {

    private Long id;

    private String name;

    private Long currentGameId;

}
