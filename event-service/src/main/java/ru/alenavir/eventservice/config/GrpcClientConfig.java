package ru.alenavir.eventservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alenavir.gameservice.grpc.GameServiceGrpc;
import ru.alenavir.playerservice.grpc.PlayerServiceGrpc;
import ru.alenavir.unitservice.grpc.UnitServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Value("${services.player.host}")
    private String playerHost;
    @Value("${services.player.port}")
    private int playerPort;

    @Value("${services.unit.host}")
    private String unitHost;
    @Value("${services.unit.port}")
    private int unitPort;

    @Value("${services.game.host}")
    private String gameHost;
    @Value("${services.game.port}")
    private int gamePort;

    private ManagedChannel playerChannel;
    private ManagedChannel unitChannel;
    private ManagedChannel gameChannel;

    @Bean(name = "playerChannel")
    public ManagedChannel playerChannel() {
        this.playerChannel = ManagedChannelBuilder
                .forAddress(playerHost, playerPort)
                .usePlaintext()
                .build();
        return this.playerChannel;
    }

    @Bean
    public PlayerServiceGrpc.PlayerServiceBlockingStub playerStub(
            @Qualifier("playerChannel") ManagedChannel channel) {
        return PlayerServiceGrpc.newBlockingStub(channel);
    }

    @Bean(name = "unitChannel")
    public ManagedChannel unitChannel() {
        this.unitChannel = ManagedChannelBuilder
                .forAddress(unitHost, unitPort)
                .usePlaintext()
                .build();
        return this.unitChannel;
    }

    @Bean
    public UnitServiceGrpc.UnitServiceBlockingStub unitStub(
            @Qualifier("unitChannel") ManagedChannel channel) {
        return UnitServiceGrpc.newBlockingStub(channel);
    }

    @Bean(name = "gameChannel")
    public ManagedChannel gameChannel() {
        this.gameChannel = ManagedChannelBuilder
                .forAddress(gameHost, gamePort)
                .usePlaintext()
                .build();
        return this.gameChannel;
    }

    @Bean
    public GameServiceGrpc.GameServiceBlockingStub gameStub(
            @Qualifier("gameChannel") ManagedChannel channel) {
        return GameServiceGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (playerChannel != null) playerChannel.shutdown();
        if (unitChannel != null) unitChannel.shutdown();
        if (gameChannel != null) gameChannel.shutdown();
    }
}