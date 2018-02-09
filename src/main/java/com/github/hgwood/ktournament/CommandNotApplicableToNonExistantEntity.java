package com.github.hgwood.ktournament;

import lombok.Value;

@Value
public class CommandNotApplicableToNonExistantEntity implements DomainEvent {
    Command command;
}
