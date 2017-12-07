package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class TournamentOpened implements Event {
    UUID tournament;
    int maxPlayersPerTable;
    int maxTables;
}
