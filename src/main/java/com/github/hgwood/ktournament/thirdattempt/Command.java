package com.github.hgwood.ktournament.thirdattempt;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "JOIN_TOURNAMENT", value = JoinTournamentCommand.class)
})
public interface Command {
    String getId();
}
