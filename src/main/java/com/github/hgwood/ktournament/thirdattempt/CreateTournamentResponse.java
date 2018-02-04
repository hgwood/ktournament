package com.github.hgwood.ktournament.thirdattempt;

import lombok.Value;

public interface CreateTournamentResponse {
    @Value
    class TournamentCreated implements CreateTournamentResponse, Event {
        String tournamentId;
        String name;
        int maxPlayers;
    }

}
