package ru.alenavir.gameservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alenavir.playerservice.grpc.PlayerServiceGrpc;

@Configuration
public class GrpcClientConfig {

    @Bean
    public ManagedChannel playerChannel() {
        return ManagedChannelBuilder
                .forAddress("localhost", 9090) // PlayerService порт
                .usePlaintext()
                .build();
    }

    @Bean
    public PlayerServiceGrpc.PlayerServiceBlockingStub playerStub(ManagedChannel channel) {
        return PlayerServiceGrpc.newBlockingStub(channel);
    }
}