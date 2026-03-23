package ru.alenavir.unitservice.service.kafka.topic;


import java.util.List;

public interface EventRoutingStrategy {
    boolean supports(String eventType);
    List<String> getTopics();
}
