package com.github.hgwood.ktournament.secondattempt;

import lombok.Value;

import java.util.UUID;

@Value
public class TogglePlayerIsReadyRejected implements Event {
    UUID tournament;
    UUID player;
    String reason;
}
