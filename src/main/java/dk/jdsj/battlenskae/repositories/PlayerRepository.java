package dk.jdsj.battlenskae.repositories;


import dk.jdsj.battlenskae.entities.Player;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends CrudRepository<Player, Integer> {

    @NonNull List<Player> findAll();
    Player findPlayerBySnakeName(String snakeName);
}
