package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class StateEnvelope<T extends State> {
    long version;
    UUID emittingEventId;
    T payload;

    public static <T extends State> StateEnvelope<T> zero() {
        return new StateEnvelope<>(0, null, null);
    }

    public StateEnvelope<T> next(UUID emittingEventId, T payload) {
        return new StateEnvelope<>(this.version + 1, emittingEventId, payload);
    }
}
