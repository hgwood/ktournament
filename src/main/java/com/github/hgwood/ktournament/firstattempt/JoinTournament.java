package com.github.hgwood.ktournament.firstattempt;

import lombok.Data;

import java.util.UUID;

@Data
public class JoinTournament {
    private final UUID tournament;
    private final String playerName;
}
