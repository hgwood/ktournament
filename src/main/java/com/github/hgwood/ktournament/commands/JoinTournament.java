package com.github.hgwood.ktournament.commands;

import com.github.hgwood.ktournament.*;
import com.github.hgwood.ktournament.events.CommandNotApplicableToNonExistantEntity;
import com.github.hgwood.ktournament.events.TournamentIsFull;
import com.github.hgwood.ktournament.events.TournamentIsNotAcceptingPlayers;
import com.github.hgwood.ktournament.events.TournamentJoinedByPlayer;
import io.vavr.collection.List;
import lombok.Value;

import java.util.UUID;

@Value
public class JoinTournament implements Command<TournamentJoiningState> {
    UUID playerId;

    @Override
    public List<Event> decide(TournamentJoiningState state) {
        if (state == null) {
            return List.of(new CommandNotApplicableToNonExistantEntity());
        } else if (!state.isAcceptingPlayers()) {
            return List.of(new TournamentIsNotAcceptingPlayers());
        } else if (state.getPlayersJoined() >= state.getMaxPlayers()) {
            return List.of(new TournamentIsFull(this.playerId, state.getPlayersJoined(), state.getMaxPlayers()));
        } else {
            int newPlayersJoined = state.getPlayersJoined() + 1;
            return List.of(
                new TournamentJoinedByPlayer(
                    this.playerId,
                    state.getPlayersJoined(),
                    newPlayersJoined,
                    newPlayersJoined == state.getMaxPlayers()
                )
            );
        }
    }
}
