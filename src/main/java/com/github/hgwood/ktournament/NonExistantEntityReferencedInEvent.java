package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class NonExistantEntityReferencedInEvent implements DomainEvent {
    UUID id;
    Event event;
}
