package com.github.hgwood.ktournament.firstattempt;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

@RequiredArgsConstructor
public class OpenTournamentProcessor extends NoopCommandProcessor {
    @Override
    public List<? extends Event> process(OpenTournament command) {
        UUID id = UUID.randomUUID();
        return asList(new TournamentOpened(id, command.getMaxPlayersPerTable(), command.getMaxTables()));
    }
}
