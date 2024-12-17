package dk.jdsj.battlenskae.controllers;

import dk.jdsj.battlenskae.Services.MatchService;
import dk.jdsj.battlenskae.Services.TournamentService;
import dk.jdsj.battlenskae.entities.Tournament;
import dk.jdsj.battlenskae.models.TournamentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final MatchService matchService;
    private final TaskExecutor taskExecutor;

    @CrossOrigin(origins = "*")
    @PostMapping("/tournament")
    public Tournament createTournament(@RequestBody TournamentRequest tournament) {
        return tournamentService.saveTournament(tournament);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/tournament/{id}/generate-matches")
    public Tournament generateMatches(@PathVariable int id) {
        return tournamentService.generateMatches(id);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/tournament/{tournamentId}/player/{playerId}")
    public Tournament addPlayerToTournament(@PathVariable int tournamentId, @PathVariable int playerId) {
        return tournamentService.addPlayerToTournament(tournamentId, playerId);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/tournament/{id}")
    public Tournament getTournament(@PathVariable int id) {
        return tournamentService.getTournamentById(id);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/tournament/{id}/start-game")
    public ResponseEntity<Void> startGame(@PathVariable int id) {
        // Run the game asynchronously
        taskExecutor.execute(() -> matchService.startGame(id));

        // Return 200 OK immediately
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/tournament/{id}/advance-round")
    public Tournament advanceRound(@PathVariable int id) {
        return tournamentService.advanceWinnersToNextRound(id);
    }
}
