package com.github.hgwood.ktournament;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "OPEN_TOURNAMENT_TO_PLAYERS", value = TournamentJoinLogic.OpenTournamentToPlayers.class)
})
public interface Command {
    UUID getId();
}
