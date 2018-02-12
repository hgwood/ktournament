package com.github.hgwood.ktournament.events;

import com.github.hgwood.ktournament.Event;
import com.github.hgwood.ktournament.TournamentJoiningState;
import lombok.Value;

import java.util.UUID;

@Value
public class TournamentJoinedByPlayer implements Event<TournamentJoiningState> {
    UUID playerId;
    int playersBefore;
    int playersAfter;
    boolean tournamentIsNowFull;

    @Override
    public TournamentJoiningState evolve(TournamentJoiningState state) {
        return state.toBuilder().playersJoined(this.playersAfter).build();
    }
}
