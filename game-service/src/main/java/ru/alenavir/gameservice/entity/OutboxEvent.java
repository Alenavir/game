package ru.alenavir.gameservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType; // e.g. "PLAYER"
    private String aggregateId;   // id игрока

    private String eventType;     // e.g. "PLAYER_JOINED_GAME"

    @Column(columnDefinition = "CLOB")
    private String payload;       // JSON с данными

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean published = false;
}
