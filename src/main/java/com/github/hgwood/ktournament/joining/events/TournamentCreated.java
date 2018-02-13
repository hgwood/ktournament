package com.github.hgwood.ktournament.joining.events;

import com.github.hgwood.ktournament.joining.TournamentJoiningEvent;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import lombok.Value;

@Value
public class TournamentCreated implements TournamentJoiningEvent {
    int maxPlayers;

    @Override
    public TournamentJoiningState evolve(TournamentJoiningState state) {
        return TournamentJoiningState.builder()
            .acceptingPlayers(true)
            .playersJoined(0)
            .maxPlayers(maxPlayers)
            .build();
    }
}
