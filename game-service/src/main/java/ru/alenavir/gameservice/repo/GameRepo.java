package ru.alenavir.gameservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alenavir.gameservice.entity.Game;

import java.util.Optional;

public interface GameRepo extends JpaRepository<Game, Long> {
    @Query("SELECT g FROM Game g JOIN FETCH g.playerIds WHERE g.id = :id")
    Optional<Game> findByIdWithPlayers(@Param("id") Long id);
}
