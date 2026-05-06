package ru.alenavir.gameservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alenavir.playerservice.grpc.PlayerServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Value("${player.service.grpc.host:localhost}")
    private String playerHost;

    @Value("${player.service.grpc.port:9091}")
    private int playerPort;

    private ManagedChannel playerChannel;

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

    @PreDestroy
    public void shutdown() {
        if (playerChannel != null) {
            playerChannel.shutdown();
        }
    }
}