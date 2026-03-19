package ru.alenavir.gameservice.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alenavir.gameservice.grpc.GameGrpcService;

import java.io.IOException;

@Configuration
@Slf4j
public class GrpcServerConfig {

    private Server server;

    private final GameGrpcService gameGrpcService;

    public GrpcServerConfig(GameGrpcService gameGrpcService) {
        this.gameGrpcService = gameGrpcService;
    }

    @Bean
    public Server grpcServer() throws IOException {
        server = ServerBuilder
                .forPort(9091) // порт GameService
                .addService(gameGrpcService)
                .build()
                .start();

        log.info("Game gRPC Server started on port {}", 9091);

        // graceful shutdown при завершении приложения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Game gRPC server");
            if (server != null) {
                server.shutdown();
            }
        }));

        new Thread(() -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                log.error("Game gRPC server interrupted", e);
            }
        }).start();

        return server;
    }
}
