package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class JoinTournament implements Event {
    UUID tournament;
    UUID player;
}
