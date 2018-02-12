package com.github.hgwood.ktournament.commands;

import com.github.hgwood.ktournament.Command;
import com.github.hgwood.ktournament.Event;
import com.github.hgwood.ktournament.TournamentJoiningState;
import com.github.hgwood.ktournament.events.TournamentStarted;
import com.github.hgwood.ktournament.events.NotEnoughPlayersInTournamentToStart;
import io.vavr.collection.List;
import lombok.Value;

import java.util.UUID;

@Value
public class StartTournament implements Command<TournamentJoiningState> {
    public static int MINIMUM_PLAYERS_REQUIRED_TO_START = 2;

    UUID id;

    @Override
    public List<Event> decide(TournamentJoiningState state) {
        if (state.getPlayersJoined() < MINIMUM_PLAYERS_REQUIRED_TO_START) {
            return List.of(new NotEnoughPlayersInTournamentToStart(state.getPlayersJoined()));
        } else {
            return List.of(new TournamentStarted());
        }
    }
}
