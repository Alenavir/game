package ru.alenavir.eventservice.service.netty.handler.player;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlayerCreateCommand(
        @NotNull @Size(min = 1, max = 255) String name
) {
}
