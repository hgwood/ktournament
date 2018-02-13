package com.github.hgwood.ktournament.joining.events;

import com.github.hgwood.ktournament.joining.TournamentJoiningEvent;
import lombok.Value;

import java.util.UUID;

@Value
public class TournamentIsFull implements TournamentJoiningEvent {
    UUID playerId;
    int playersJoined;
    int maxPlayers;
}
