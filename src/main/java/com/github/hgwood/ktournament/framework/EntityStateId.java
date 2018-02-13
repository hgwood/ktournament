package com.github.hgwood.ktournament.framework;

import lombok.Value;

import java.util.UUID;

@Value
public class EntityStateId {
    UUID entityId;
    long version;

    public EntityStateId next() {
        return new EntityStateId(this.entityId, this.version + 1);
    }
}
