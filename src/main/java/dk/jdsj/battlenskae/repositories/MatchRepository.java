package dk.jdsj.battlenskae.repositories;

import dk.jdsj.battlenskae.entities.Match;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends CrudRepository<Match, Integer> {

    @Query(value = """
       SELECT m.*
       FROM match m
       JOIN round r ON m.round_id = r.id
       JOIN tournament_rounds tr ON r.id = tr.rounds_id
       JOIN tournament t ON tr.tournament_id = t.id
       WHERE t.id = :tournamentId AND r.round_number = :roundNumber
       """, nativeQuery = true)
    List<Match> findAllByRoundAndTournament(@Param("roundNumber") int roundNumber, @Param("tournamentId") int tournamentId);
}
