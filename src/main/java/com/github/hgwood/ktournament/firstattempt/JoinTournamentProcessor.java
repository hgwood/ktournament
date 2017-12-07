package com.github.hgwood.ktournament.firstattempt;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

@RequiredArgsConstructor
public class JoinTournamentProcessor extends NoopCommandProcessor {
    private final Store<UUID, Tournament> tournaments;

    @Override
    public List<? extends Event> process(JoinTournament command) {
        if (tournaments.has(command.getTournament())) {
            return asList(new PlayerRequestsJoin(command.getTournament(), command.getPlayerName()));
        } else {
            return asList(new TournamentNotFound(command.getTournament()));
        }
    }
}
