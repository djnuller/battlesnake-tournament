package dk.jdsj.battlenskae.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tournament")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tournament_seq")
    @SequenceGenerator(name = "tournament_seq", sequenceName = "tournament_id_seq", allocationSize = 1)
    private int id;

    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    List<Player> players;

    @OneToMany(fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<Round> rounds;
}
