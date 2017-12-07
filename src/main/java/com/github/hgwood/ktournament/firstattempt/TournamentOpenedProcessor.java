package com.github.hgwood.ktournament.firstattempt;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class TournamentOpenedProcessor extends NoopEventProcessor {
    private final Store<UUID, Tournament> tournaments;

    @Override
    public void process(TournamentOpened event) {
        tournaments.put(event.getId(), new Tournament(event.getId(), event.getMaxPlayersPerTable(), event.getMaxTables()));
    }
}
