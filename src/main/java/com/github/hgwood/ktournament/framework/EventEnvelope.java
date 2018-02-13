package com.github.hgwood.ktournament.framework;

import lombok.Value;

import java.util.UUID;

@Value
public class EventEnvelope<T extends State> {
    UUID id;
    UUID commandId;
    Event<T> payload;
}
