package com.github.hgwood.ktournament.joining.commands;

import com.github.hgwood.ktournament.framework.Event;
import com.github.hgwood.ktournament.joining.TournamentJoiningCommand;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import com.github.hgwood.ktournament.joining.events.AttemptToOverwriteAnExistingTournament;
import com.github.hgwood.ktournament.joining.events.TournamentCreated;
import io.vavr.collection.List;
import lombok.Value;

@Value
public class OpenTournamentToPlayers implements TournamentJoiningCommand {
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
