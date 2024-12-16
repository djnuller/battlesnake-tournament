package dk.jdsj.battlenskae.Services;

import dk.jdsj.battlenskae.entities.Player;
import dk.jdsj.battlenskae.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player getPlayerById(int id) {
        return playerRepository.findById(id).orElseThrow();
    }

    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }

    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    public void deletePlayer(int id) {
        playerRepository.deleteById(id);
    }

    public Player getPlayerBySnakeName(String snakeName) {
        return playerRepository.findPlayerBySnakeName(snakeName);
    }
}
