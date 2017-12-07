package com.github.hgwood.ktournament.firstattempt;

import lombok.Data;

import java.util.UUID;

@Data
public class TournamentNotFound implements Event {
    private final UUID id;

    public void accept(EventProcessor eventProcessor) {
        eventProcessor.process(this);
    }
}
