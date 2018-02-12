package com.github.hgwood.ktournament;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TournamentJoiningState implements State {
    boolean acceptingPlayers;
    int playersJoined;
    int maxPlayers;
}
