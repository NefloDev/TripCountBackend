package neflo.dev.repository;

import neflo.dev.model.entity.TripModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<TripModel, UUID> {
}
