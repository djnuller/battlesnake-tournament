package dk.jdsj.battlenskae.Services;

import dk.jdsj.battlenskae.entities.Match;
import dk.jdsj.battlenskae.entities.Player;
import dk.jdsj.battlenskae.entities.Round;
import dk.jdsj.battlenskae.entities.Tournament;
import dk.jdsj.battlenskae.models.TournamentRequest;
import dk.jdsj.battlenskae.repositories.MatchRepository;
import dk.jdsj.battlenskae.repositories.RoundRepository;
import dk.jdsj.battlenskae.repositories.TournamentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final RoundRepository roundRepository;
    private final PlayerService playerService;

    public Tournament saveTournament(TournamentRequest tournament) {

        var players = tournament.getPlayerIds().stream().map(playerService::getPlayerById).toList();

        var tournamentEntity = Tournament.builder()
                .players(players)
                .build();

        return tournamentRepository.save(tournamentEntity);
    }

    public Tournament addPlayerToTournament(int tournamentId, int playerId) {
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        tournament.getPlayers().stream().findAny().ifPresent(player -> {
            if (player.getId() == playerId) {
                throw new IllegalArgumentException("Player is already in the tournament");
            }
        });

        var player = playerService.getPlayerById(playerId);
        tournament.getPlayers().add(player);
        return tournamentRepository.save(tournament);
    }

    private List<List<Integer>> sortPlayersIntoMatches(int numPlayers, boolean isOneVsOne) {
        log.info("Generating match structure for " + numPlayers + " players");
        List<List<Integer>> rounds = new ArrayList<>();

        while (numPlayers > 1) {
            List<Integer> matches = getMatchesForRound(numPlayers, isOneVsOne);
            log.info("Round with " + matches.size() + " matches: " + matches);

            rounds.add(matches);
            numPlayers = matches.stream().mapToInt(match -> (int) Math.ceil(match / 2.0)).sum(); // Half players advance

            if (matches.size() == 2) {
                break; // Stop when only one match remains
            }
        }

        // add the final match if there is not round with only 1 match using stream and filter
        var matches = rounds.stream().filter(round -> round.size() == 1).findFirst();
        if (matches.isEmpty()) {
            rounds.add(Collections.singletonList(4));
        }

        // order the rounds so the round with most matches is first
        var orderedRounds = new ArrayList<List<Integer>>();
        rounds.stream().sorted((r1, r2) -> r2.size() - r1.size()).forEach(orderedRounds::add);

        log.info("Final match structure: " + orderedRounds);
        return orderedRounds;
    }

    private static List<Integer> getMatchesForRound(int numPlayers, boolean isOneVsOne) {
        List<Integer> matches = new ArrayList<>();


        if (isOneVsOne) {
            // Create 1v1 matches for all players
            for (int i = 0; i < numPlayers / 2; i++) {
                matches.add(2);
            }
            if (numPlayers % 2 != 0) {
                matches.add(1); // Handle odd player out
            }
        } else {
            if (numPlayers % 4 == 0) {
                for (int i = 0; i < numPlayers / 4; i++) {
                    matches.add(4);
                }
            } else if (numPlayers % 4 == 3) {
                for (int i = 0; i < numPlayers / 4; i++) {
                    matches.add(4);
                }
                matches.add(3);
            } else if (numPlayers % 4 == 2) {
                for (int i = 0; i < (numPlayers - 6) / 4; i++) {
                    matches.add(4);
                }
                matches.add(3);
                matches.add(3);
            } else { // numPlayers % 4 == 1
                for (int i = 0; i < (numPlayers - 9) / 4; i++) {
                    matches.add(4);
                }
                matches.add(3);
                matches.add(3);
                matches.add(3);
            }
        }


        log.info("Matches: {}", matches);
        return matches;
    }

    @Transactional
    public Tournament generateMatches(int tournamentId, boolean isOneVsOne) {
        // Retrieve the tournament from the database
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();
        List<Player> players = tournament.getPlayers();
        int numPlayers = players.size();

        Collections.shuffle(players, new Random());

        // Generate the match structure for the tournament rounds
        List<List<Integer>> matchStructure = this.sortPlayersIntoMatches(numPlayers, isOneVsOne);

        List<Round> rounds = new ArrayList<>();
        int roundNumber = 1;

        for (List<Integer> matchCounts : matchStructure) {
            List<Match> matches = new ArrayList<>();

            Collections.shuffle(players, new Random());

            for (Integer matchSize : matchCounts) {
                Match match;
                // For the first round, distribute players into matches
                if (roundNumber == 1 && !players.isEmpty()) {
                    List<Player> matchPlayers = players.subList(0, Math.min(matchSize, players.size()));
                    match = Match.builder()
                            .players(new ArrayList<>(matchPlayers))
                            .build();
                    players = players.subList(matchPlayers.size(), players.size());
                } else {
                    // For subsequent rounds, create empty matches
                    match = Match.builder()
                            .players(new ArrayList<>())
                            .build();
                }

                // Save the match and add it to the match list
                match = matchRepository.save(match);
                matches.add(match);
            }

            // Create the round and associate matches
            Round round = Round.builder()
                    .roundNumber(roundNumber++)
                    .playerCount(matches.stream().mapToInt(m -> m.getPlayers().size()).sum())
                    .matches(matches)
                    .build();

            // Link matches to the round
            for (Match match : matches) {
                match.setRound(round);
                matchRepository.save(match); // Update the match with its round
            }

            // Save the round and add it to the round list
            round = roundRepository.save(round);
            rounds.add(round);
        }

        // Set the generated rounds to the tournament and save it
        tournament.setRounds(rounds);

        return tournamentRepository.save(tournament);
    }

    @Transactional
    public Tournament advanceWinnersToNextRound(final int tournamentId, boolean isOneVSOne) {
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();
        var rounds = tournament.getRounds();

        // Find the most recent completed round
        var lastCompletedRound = rounds.stream()
                .filter(round -> round.getMatches().stream()
                        .allMatch(match -> match.getWinnerPlayerId() > 0))
                .reduce((first, second) -> second) // Get the last completed round
                .orElseThrow(() -> new IllegalStateException("No completed rounds found."));

        // Get the next round that has no players assigned
        var nextRound = rounds.stream()
                .filter(round -> round.getRoundNumber() == lastCompletedRound.getRoundNumber() + 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No next round available to advance to."));

        if (nextRound.getMatches().stream().anyMatch(match -> match.getPlayers() != null && !match.getPlayers().isEmpty())) {
            throw new IllegalStateException("Next round already has players assigned.");
        }

        // Gather advancing players from the last completed round
        var advancingPlayers = lastCompletedRound.getMatches().stream()
                .flatMap(match -> {
                    if (isOneVSOne) {
                        // Only winner advances in 1v1 tournaments
                        return match.getPlayers().stream()
                                .filter(player -> player.getId() == match.getWinnerPlayerId());
                    } else {
                        // Winners and second-place players advance in non-1v1 tournaments
                        return match.getPlayers().stream()
                                .filter(player -> player.getId() == match.getWinnerPlayerId() || player.getId() == match.getSecondPlacePlayerId());
                    }
                })
                .distinct()
                .sorted(Comparator.comparing(Player::getId)) // Ensure players are sorted by ID
                .toList();

        nextRound.setPlayerCount(advancingPlayers.size());

        // Distribute players to the matches in the next round
        distributePlayersToMatchesByClosestID(advancingPlayers, nextRound.getMatches());

        return tournamentRepository.save(tournament);
    }

    private static void distributePlayersToMatchesByClosestID(List<Player> players, List<Match> matches) {
        int matchIndex = 0;

        // Loop through players and assign them to the matches by closest ID logic
        for (Player player : players) {
            Match match = matches.get(matchIndex);

            if (match.getPlayers() == null) {
                match.setPlayers(new ArrayList<>());
            }

            match.getPlayers().add(player);

            // Move to the next match, wrap around if at the end
            matchIndex = (matchIndex + 1) % matches.size();
        }

        // Ensure matches are sorted by their index
        matches.sort(Comparator.comparing(Match::getId));
    }
    public Tournament getTournamentById(int id) {
        return tournamentRepository.findWithRoundsById(id);
    }

    public Tournament findWithRounds(int id) {
        return tournamentRepository.findWithRoundsById(id);
    }

}
