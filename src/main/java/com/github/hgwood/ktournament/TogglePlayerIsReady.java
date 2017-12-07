package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class TogglePlayerIsReady implements Event {
    UUID tournament;
    UUID player;
}
