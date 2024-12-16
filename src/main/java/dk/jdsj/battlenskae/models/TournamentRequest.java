package dk.jdsj.battlenskae.models;

import lombok.Data;

import java.util.List;

@Data
public class TournamentRequest {

    List<Integer> playerIds;
}
