package ru.alenavir.gameservice.service.kafka.topic;

import ru.alenavir.gameservice.entity.enums.EventType;

public interface EventRoutingStrategy {
    boolean supports(EventType eventType); // проверка, подходит ли стратегия для события
    String getTopic(); // топик, куда отправлять
}
