package ru.alenavir.unitservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.unitservice.entity.OutboxEvent;

import java.util.List;

public interface OutboxRepo extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop100ByPublishedFalse();
}
