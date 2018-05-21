package com.github.hgwood.ktournament.joining;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.hgwood.ktournament.framework.Command;
import com.github.hgwood.ktournament.joining.commands.JoinTournament;
import com.github.hgwood.ktournament.joining.commands.OpenTournamentToPlayers;
import com.github.hgwood.ktournament.joining.commands.StartTournament;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "OPEN_TOURNAMENT_TO_PLAYERS", value = OpenTournamentToPlayers.class),
    @JsonSubTypes.Type(name = "JOIN_TOURNAMENT", value = JoinTournament.class),
    @JsonSubTypes.Type(name = "START_TOURNAMENT", value = StartTournament.class)
})
public interface TournamentJoiningCommand extends Command<TournamentJoiningState> {

}
