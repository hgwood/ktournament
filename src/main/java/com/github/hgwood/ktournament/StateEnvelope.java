package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class StateEnvelope<T extends State> {
    long version;
    UUID emmittingEventId;
    T payload;

    public static <T extends State> StateEnvelope<T> zero() {
        return new StateEnvelope<>(0, null, (T) null);
    }

    public StateEnvelope<T> next(UUID emmittingEventId, T payload) {
        return new StateEnvelope<>(this.version + 1, emmittingEventId, payload);
    }
}
