package com.github.hgwood.ktournament.framework;

import lombok.Value;

import java.util.UUID;

@Value
public class CommandEnvelope<T extends State> {
    UUID id;
    Command<T> payload;
}
