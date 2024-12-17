package dk.jdsj.battlenskae.controllers;

import dk.jdsj.battlenskae.Services.PlayerService;
import dk.jdsj.battlenskae.entities.Player;
import dk.jdsj.battlenskae.models.PlayerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @CrossOrigin(origins = "*")
    @GetMapping("/player/{id}")
    public Player getPlayer(@PathVariable int id) {
        return playerService.getPlayerById(id);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/players")
    public List<Player> getPlayers() {
        return playerService.getPlayers();
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/player/{id}")
    public void deletePlayer(@PathVariable int id) {
        playerService.deletePlayer(id);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/player")
    public void savePlayer(@RequestBody PlayerRequest player) {

        var playerEntity = Player.builder()
                .name(player.getName())
                .snakeName(player.getSnakeName())
                .snakeUrl(player.getSnakeUrl())
                .build();

        playerService.savePlayer(playerEntity);
    }
}
