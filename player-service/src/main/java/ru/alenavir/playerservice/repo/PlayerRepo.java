package ru.alenavir.playerservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.playerservice.entity.Player;

public interface PlayerRepo extends JpaRepository<Player, Long> {
    boolean existsByIdAndCurrentGameIdIsNotNull(Long id);
}
