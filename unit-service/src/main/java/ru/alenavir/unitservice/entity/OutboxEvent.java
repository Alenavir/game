package ru.alenavir.unitservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.alenavir.unitservice.entity.enums.EventType;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType; // e.g. "UNIT"
    private String aggregateId;   // id юнита

    @Enumerated(EnumType.STRING)
    private EventType eventType;     // e.g. "UNIT_CREATED"

    @Column(columnDefinition = "CLOB")
    private String payload;       // JSON с данными

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean published = false;
}
