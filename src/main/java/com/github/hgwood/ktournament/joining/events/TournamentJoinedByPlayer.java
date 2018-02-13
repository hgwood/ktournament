package com.github.hgwood.ktournament.joining.events;

import com.github.hgwood.ktournament.joining.TournamentJoiningEvent;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import lombok.Value;

import java.util.UUID;

@Value
public class TournamentJoinedByPlayer implements TournamentJoiningEvent {
    UUID playerId;
    int playersBefore;
    int playersAfter;
    boolean tournamentIsNowFull;

    @Override
    public TournamentJoiningState evolve(TournamentJoiningState state) {
        return state.toBuilder().playersJoined(this.playersAfter).build();
    }
}
