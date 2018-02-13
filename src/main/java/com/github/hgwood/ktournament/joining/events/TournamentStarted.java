package com.github.hgwood.ktournament.joining.events;

import com.github.hgwood.ktournament.joining.TournamentJoiningEvent;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import lombok.Value;

@Value
public class TournamentStarted implements TournamentJoiningEvent {

    @Override
    public TournamentJoiningState evolve(TournamentJoiningState state) {
        return state.toBuilder().acceptingPlayers(false).build();
    }
}
