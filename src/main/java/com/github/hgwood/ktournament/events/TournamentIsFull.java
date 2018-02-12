package com.github.hgwood.ktournament.events;

import com.github.hgwood.ktournament.Event;
import com.github.hgwood.ktournament.TournamentJoiningState;
import lombok.Value;

import java.util.UUID;

@Value
public class TournamentIsFull implements Event<TournamentJoiningState> {
    UUID playerId;
    int playersJoined;
    int maxPlayers;
}
