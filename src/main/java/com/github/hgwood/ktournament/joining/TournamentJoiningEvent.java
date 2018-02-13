package com.github.hgwood.ktournament.joining;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.hgwood.ktournament.framework.Event;
import com.github.hgwood.ktournament.joining.events.AttemptToOverwriteAnExistingTournament;
import com.github.hgwood.ktournament.joining.events.TournamentCreated;
import com.github.hgwood.ktournament.joining.events.TournamentIsFull;
import com.github.hgwood.ktournament.joining.events.TournamentJoinedByPlayer;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "TOURNAMENT_CREATED", value = TournamentCreated.class),
    @JsonSubTypes.Type(name = "TOURNAMENT_JOINED_BY_PLAYERS", value = TournamentJoinedByPlayer.class),
    @JsonSubTypes.Type(name = "TOURNAMENT_IS_FULL", value = TournamentIsFull.class),
    @JsonSubTypes.Type(name = "ATTEMPT_TO_OVERWRITE_AN_EXISTING_TOURNAMENT", value = AttemptToOverwriteAnExistingTournament.class)
})
public interface TournamentJoiningEvent extends Event<TournamentJoiningState> {

}
