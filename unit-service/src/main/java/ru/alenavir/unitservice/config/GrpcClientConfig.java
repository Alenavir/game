package ru.alenavir.unitservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alenavir.gameservice.grpc.GameServiceGrpc;
import ru.alenavir.playerservice.grpc.PlayerServiceGrpc;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class GrpcClientConfig {

    private ManagedChannel playerChannel;
    private ManagedChannel gameChannel;

    // ===== Player Service =====
    @Bean(name = "playerChannel")
    public ManagedChannel playerChannel() {
        this.playerChannel = ManagedChannelBuilder
                .forAddress("localhost", 9091)
                .usePlaintext()
                .build();
        return this.playerChannel;
    }

    @Bean
    public PlayerServiceGrpc.PlayerServiceBlockingStub playerStub(
            @Qualifier("playerChannel") ManagedChannel channel) {
        return PlayerServiceGrpc.newBlockingStub(channel);
    }

    // ===== Game Service =====
    @Bean(name = "gameChannel")
    public ManagedChannel gameChannel() {
        this.gameChannel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        return this.gameChannel;
    }

    @Bean
    public GameServiceGrpc.GameServiceBlockingStub gameStub(
            @Qualifier("gameChannel") ManagedChannel channel) {
        return GameServiceGrpc.newBlockingStub(channel);
    }

    // ===== Graceful shutdown =====
    @PreDestroy
    public void shutdown() {
        if (playerChannel != null) {
            playerChannel.shutdown();
        }
        if (gameChannel != null) {
            gameChannel.shutdown();
        }
    }
}