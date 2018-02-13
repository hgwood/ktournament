package com.github.hgwood.ktournament.joining.commands;

import com.github.hgwood.ktournament.framework.Event;
import com.github.hgwood.ktournament.joining.TournamentJoiningCommand;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import com.github.hgwood.ktournament.joining.events.NotEnoughPlayersInTournamentToStart;
import com.github.hgwood.ktournament.joining.events.TournamentStarted;
import io.vavr.collection.List;
import lombok.Value;

@Value
public class StartTournament implements TournamentJoiningCommand {
    public static int MINIMUM_PLAYERS_REQUIRED_TO_START = 2;

    @Override
    public List<Event> decide(TournamentJoiningState state) {
        if (state.getPlayersJoined() < MINIMUM_PLAYERS_REQUIRED_TO_START) {
            return List.of(new NotEnoughPlayersInTournamentToStart(state.getPlayersJoined()));
        } else {
            return List.of(new TournamentStarted());
        }
    }
}
