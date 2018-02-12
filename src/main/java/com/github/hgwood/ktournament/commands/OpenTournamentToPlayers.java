package com.github.hgwood.ktournament.commands;

import com.github.hgwood.ktournament.Command;
import com.github.hgwood.ktournament.Event;
import com.github.hgwood.ktournament.events.TournamentCreated;
import com.github.hgwood.ktournament.TournamentJoiningState;
import com.github.hgwood.ktournament.events.AttemptToOverwriteAnExistingTournament;
import io.vavr.collection.List;
import lombok.Value;

import java.util.UUID;

@Value
public class OpenTournamentToPlayers implements Command<TournamentJoiningState> {
    UUID id;
    int maxPlayers;

    @Override
    public List<Event> decide(TournamentJoiningState state) {
        if (state == null) {
            return List.of(new TournamentCreated(maxPlayers));
        } else {
            return List.of(new AttemptToOverwriteAnExistingTournament());
        }
    }
}
