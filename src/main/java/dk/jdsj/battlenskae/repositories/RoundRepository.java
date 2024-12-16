package dk.jdsj.battlenskae.repositories;

import dk.jdsj.battlenskae.entities.Round;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundRepository extends CrudRepository<Round, Integer> {
}
