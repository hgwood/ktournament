package com.github.hgwood.ktournament;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "TOURNAMENT_CREATED", value = TournamentJoinLogic.TournamentCreated.class),
    @JsonSubTypes.Type(name = "TOURNAMENT_JOINED_BY_PLAYERS", value = TournamentJoinLogic.TournamentJoinedByPlayer.class),
    @JsonSubTypes.Type(name = "TOURNAMENT_IS_FULL", value = TournamentJoinLogic.TournamentIsFull.class)
})
public interface DomainEvent {

}
