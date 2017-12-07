package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class PlayerReadinessToggled implements Event {
    UUID tournament;
    UUID player;
    boolean ready;
}
