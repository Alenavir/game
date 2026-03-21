package ru.alenavir.unitservice.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.alenavir.unitservice.entity.OutboxEvent;
import ru.alenavir.unitservice.repo.OutboxRepo;
import ru.alenavir.unitservice.service.kafka.topic.EventRouter;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxRepo outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventRouter router;

    @Scheduled(fixedDelay = 1000)
    public void process() {

        List<OutboxEvent> events = outboxRepo.findTop100ByPublishedFalse();

        for (OutboxEvent event : events) {
            String topic = router.resolveTopic(event.getEventType());

            kafkaTemplate.send(topic, event.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка при отправке события id={}", event.getId(), ex);
                    } else {
                        event.setPublished(true);
                        outboxRepo.save(event);
                        log.info("Событие успешно отправлено: id={}, type={}, topic={}",
                                event.getId(),
                                event.getEventType(),
                                topic);
                    }
                });
        }
    }
}
