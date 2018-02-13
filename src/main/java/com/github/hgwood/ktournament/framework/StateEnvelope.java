package com.github.hgwood.ktournament.framework;

import lombok.Value;

import java.util.UUID;

@Value
public class StateEnvelope<T extends State> {
    EntityStateId id;
    UUID emittingEventId;
    T payload;

    public static <T extends State> StateEnvelope<T> zero(UUID entityId) {
        return new StateEnvelope<>(new EntityStateId(entityId, 0), null, null);
    }

    public StateEnvelope<T> next(UUID emittingEventId, T payload) {
        return new StateEnvelope<>(this.id.next(), emittingEventId, payload);
    }
}
