package com.github.hgwood.ktournament.events;

import com.github.hgwood.ktournament.Event;
import com.github.hgwood.ktournament.TournamentJoiningState;
import lombok.Value;

@Value
public class TournamentStarted implements Event<TournamentJoiningState> {

    @Override
    public TournamentJoiningState evolve(TournamentJoiningState state) {
        return state.toBuilder().acceptingPlayers(false).build();
    }
}
