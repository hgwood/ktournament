package com.github.hgwood.ktournament.events;

import com.github.hgwood.ktournament.Event;
import com.github.hgwood.ktournament.TournamentJoiningState;
import lombok.Value;

@Value
public class TournamentCreated implements Event<TournamentJoiningState> {
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
