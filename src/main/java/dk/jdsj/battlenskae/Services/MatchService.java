package dk.jdsj.battlenskae.Services;

import dk.jdsj.battlenskae.entities.Match;
import dk.jdsj.battlenskae.entities.Player;
import dk.jdsj.battlenskae.entities.Round;
import dk.jdsj.battlenskae.repositories.MatchRepository;
import dk.jdsj.battlenskae.repositories.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerService playerService;
    private final TournamentService tournamentService;

    @SneakyThrows
    public void startGame(int tournamentId) {
        var tournament = tournamentService.getTournamentById(tournamentId);


        // Create a fixed thread pool for running matches concurrently
        var executor = Executors.newFixedThreadPool(10);

        getNextPlayableRound(tournamentId).ifPresent(round -> {
            log.info("Starting matches for round {}", round.getRoundNumber());
            Hibernate.initialize(round.getMatches());
            round.getMatches().forEach(match -> {
                executor.submit(() -> processMatch(match.getId(), match));
            });
        });

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Match processing interrupted.", e);
        }
    }

    private void processMatch(Integer matchId, Match match) {
        try {
            var winCount = runGames(matchId, match, 0);

            // Determine the top 2 players
            var sortedPlayers = winCount.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(2)
                    .toList();

            if (!sortedPlayers.isEmpty()) {
                match.setWinnerPlayerId(sortedPlayers.get(0).getKey());
            }
            if (sortedPlayers.size() > 1) {
                match.setSecondPlacePlayerId(sortedPlayers.get(1).getKey());
            }

            // Save match
            log.info("Match {} completed: Winner: {}, Second Place: {}",
                    matchId,
                    match.getWinnerPlayerId(),
                    match.getSecondPlacePlayerId());
            matchRepository.save(match);
        } catch (Exception e) {
            log.error("Error processing match {}", matchId, e);
        }
    }

    private Map<Integer, Integer> runGames(Integer matchId, Match match, int tries) {
        if (tries == 5) {
            throw new IllegalArgumentException("Failed to run match after 5 tries");
        }

        var players = match.getPlayers();
        var winCount = players.stream().collect(Collectors.toMap(Player::getId, p -> 0));

        try {
            for (int i = 0; i < 100; i++) {
                var command = buildCommand(players);
                var output = runGame(command);
                var winner = parseWinner(output);

                if (winner != null) {
                    log.info("Match {} iteration {} winner: {}", matchId, i, winner.getSnakeName());
                    winCount.put(winner.getId(), winCount.get(winner.getId()) + 1);
                }
            }
        } catch (Exception e) {
            log.info("Error running match {}", matchId);
            return runGames(matchId, match, tries + 1);
        }

        return winCount;
    }


    private String buildCommand(List<Player> players) {
        var command = new StringBuilder("./battlesnake play -W 11 -H 11");
        players.forEach(p -> {
            command.append(" --name ").append(p.getId()).append(" --url ").append(p.getSnakeUrl());
        });
        return command.toString();
    }

    private Player parseWinner(String output) {
        var winnerRegex = "Game completed after \\d+ turns\\. (\\w+) was the winner\\.";
        var drawRegex = "Game completed after \\d+ turns\\. It was a draw\\.";
        var winnerPattern = Pattern.compile(winnerRegex);
        var drawPattern = Pattern.compile(drawRegex);

        // Check for a draw
        var drawMatcher = drawPattern.matcher(output);
        if (drawMatcher.find()) {
            return null; // Return null for a draw
        }

        // Check for a winner
        var winnerMatcher = winnerPattern.matcher(output);
        if (winnerMatcher.find()) {
            log.info("Winner: {}", winnerMatcher.group(1));
            return playerService.getPlayerById(Integer.parseInt(winnerMatcher.group(1)));
        }

        return null;
    }

    private String runGame(String command) {
        try {
            var commandParts = command.split(" ");

            ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
            processBuilder.redirectErrorStream(true); // Redirect error stream to standard output
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.lines().collect(Collectors.joining("\n"));

                if (exitCode != 0) {
                    throw new RuntimeException("Game command failed with exit code " + exitCode + ": " + output);
                }

                return output;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error running the game command", e);
        }
    }

    private Optional<Round> getNextPlayableRound(int tournamentId) {
        var tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));

        // Find the first round that has matches with players but no winners assigned
        return tournament.getRounds().stream()
                .filter(round -> round.getMatches().stream()
                        .anyMatch(match ->
                                (match.getWinnerPlayerId() == 0 || match.getSecondPlacePlayerId() == 0) &&
                                        match.getPlayers() != null && !match.getPlayers().isEmpty()))
                .findFirst();
    }

}
