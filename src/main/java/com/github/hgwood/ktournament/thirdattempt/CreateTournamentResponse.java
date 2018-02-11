package com.github.hgwood.ktournament.thirdattempt;

import lombok.Value;

public interface CreateTournamentResponse {
    @Value
    class TournamentCreated implements CreateTournamentResponse, Event {
        String tournamentId;
        String name;
        int maxPlayers;

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getEntity() {
            return null;
        }
    }

}
