package com.github.hgwood.ktournament.firstattempt;

public interface Event {
    void accept(EventProcessor eventProcessor);
}
