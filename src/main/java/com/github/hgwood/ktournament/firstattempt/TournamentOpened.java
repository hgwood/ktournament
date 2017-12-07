package com.github.hgwood.ktournament.firstattempt;

import lombok.Value;

import java.util.UUID;

@Value
public class TournamentOpened implements Event {
    private final UUID id;
    int maxPlayersPerTable;
    int maxTables;

    public void accept(EventProcessor eventProcessor) {
        eventProcessor.process(this);
    }
}
