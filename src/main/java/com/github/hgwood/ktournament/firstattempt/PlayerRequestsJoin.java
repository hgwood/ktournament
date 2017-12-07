package com.github.hgwood.ktournament.firstattempt;

import lombok.Value;

import java.util.UUID;

@Value
public class PlayerRequestsJoin implements Event {
    UUID tournament;
    String name;

    public void accept(EventProcessor eventProcessor) {
        eventProcessor.process(this);
    }
}
