package dk.jdsj.battlenskae.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Fetch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "match")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "match_seq")
    @SequenceGenerator(name = "match_seq", sequenceName = "match_id_seq", allocationSize = 1)
    private int id;

    private int winnerPlayerId;
    private int secondPlacePlayerId;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Player> players;

    @ManyToOne
    @JsonBackReference
    private Round round;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "match_player_win_count", joinColumns = @JoinColumn(name = "match_id"))
    @MapKeyJoinColumn(name = "player_id") // Link the Player entity as the map key
    @Column(name = "win_count") // Map the win count as the value
    private Map<Integer, Integer> playerWinCounts = new HashMap<>();

    public void addWin(Player player) {
        playerWinCounts.put(player.getId(), playerWinCounts.getOrDefault(player.getId(), 0) + 1);
    }

    public int getWins(Player player) {
        return playerWinCounts.getOrDefault(player.getId(), 0);
    }

}
