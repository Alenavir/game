package ru.alenavir.gameservice.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.alenavir.gameservice.dto.events.EventEnvelope;
import ru.alenavir.gameservice.entity.OutboxEvent;
import ru.alenavir.gameservice.repo.OutboxRepo;
import ru.alenavir.gameservice.service.kafka.topic.EventRouter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxRepo outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventRouter router;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    public void process() throws JsonProcessingException {

        List<OutboxEvent> events = outboxRepo.findTop100ByPublishedFalse();

            for (OutboxEvent event : events) {

                List<String> topics = router.resolveTopics(event.getEventType());

                AtomicInteger successCount = new AtomicInteger(0);

                for (String topic : topics) {

                    EventEnvelope envelope = new EventEnvelope(
                            event.getEventType(),
                            event.getAggregateType(),
                            event.getPayload()
                    );

                    String message = objectMapper.writeValueAsString(envelope);

                    kafkaTemplate.send(topic, message)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                if (successCount.incrementAndGet() == topics.size()) {
                                    event.setPublished(true);
                                    outboxRepo.save(event);
                                }
                            } else {
                                log.error("Ошибка отправки id={} topic={}", event.getId(), topic, ex);
                            }
                        });
                }
            }
    }
}
