package ru.alenavir.gameservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.alenavir.gameservice.entity.enums.GameState;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GameState state;

    @ElementCollection
    @CollectionTable(name = "game_players", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "player_id")
    private List<Long> playerIds = new ArrayList<>();
}
