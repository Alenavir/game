package ru.alenavir.unitservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alenavir.unitservice.entity.Unit;

public interface UnitRepo extends JpaRepository<Unit, Long> {
}
