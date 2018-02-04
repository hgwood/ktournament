package com.github.hgwood.ktournament;

import lombok.Value;

import java.util.UUID;

@Value
public class Event {
    UUID commandId;
    DomainEvent payload;
}
