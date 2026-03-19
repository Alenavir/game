package ru.alenavir.gameservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.gameservice.entity.Game;

public interface GameRepo extends JpaRepository<Game, Long> {
}
