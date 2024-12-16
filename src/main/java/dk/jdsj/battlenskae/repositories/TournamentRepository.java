package dk.jdsj.battlenskae.repositories;

import dk.jdsj.battlenskae.entities.Tournament;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends CrudRepository<Tournament, Integer> {

    Tournament findWithRoundsById(@Param("id") int id);
}
