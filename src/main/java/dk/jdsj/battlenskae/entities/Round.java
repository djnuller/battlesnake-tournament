package dk.jdsj.battlenskae.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Fetch;

import java.util.List;

@Entity
@Table(name = "round")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "round_seq")
    @SequenceGenerator(name = "round_seq", sequenceName = "round_id_seq", allocationSize = 1)
    private int id;

    private int roundNumber;
    private int playerCount;

    @OneToMany(fetch = FetchType.EAGER)
    @JsonManagedReference // Handle serialization of matches
    private List<Match> matches;
}
