package com.github.hgwood.ktournament.firstattempt;

import lombok.Value;

import java.util.UUID;

@Value
public class Tournament {
    UUID id;
    int maxPlayersPerTable;
    int maxTables;

}
