package com.github.hgwood.ktournament;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.hgwood.ktournament.commands.JoinTournament;
import com.github.hgwood.ktournament.commands.OpenTournamentToPlayers;
import io.vavr.collection.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "OPEN_TOURNAMENT_TO_PLAYERS", value = OpenTournamentToPlayers.class),
    @JsonSubTypes.Type(name = "JOIN_TOURNAMENT", value = JoinTournament.class)
})
public interface Command<T extends State> {
    default List<Event> decide(T state) {
        return List.empty();
    }
}
