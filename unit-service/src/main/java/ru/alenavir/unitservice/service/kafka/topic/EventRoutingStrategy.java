package ru.alenavir.unitservice.service.kafka.topic;


import ru.alenavir.unitservice.entity.enums.EventType;

public interface EventRoutingStrategy {
    boolean supports(EventType eventType); // проверка, подходит ли стратегия для события
    String getTopic(); // топик, куда отправлять
}
