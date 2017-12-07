package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class PlayerJoinedTournament implements Event {
    UUID tournament;
    UUID player;
    int table;
}
