package com.github.hgwood.ktournament.joining.commands;

import com.github.hgwood.ktournament.framework.Event;
import com.github.hgwood.ktournament.joining.TournamentJoiningCommand;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import com.github.hgwood.ktournament.joining.events.CommandNotApplicableToNonExistantEntity;
import com.github.hgwood.ktournament.joining.events.TournamentIsFull;
import com.github.hgwood.ktournament.joining.events.TournamentIsNotAcceptingPlayers;
import com.github.hgwood.ktournament.joining.events.TournamentJoinedByPlayer;
import io.vavr.collection.List;
import lombok.Value;

import java.util.UUID;

@Value
public class JoinTournament implements TournamentJoiningCommand {
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
