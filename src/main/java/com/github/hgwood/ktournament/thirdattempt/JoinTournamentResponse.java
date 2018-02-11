package com.github.hgwood.ktournament.thirdattempt;

import lombok.Getter;
import lombok.Value;

public interface JoinTournamentResponse {
    @Value
    class TournamentDoesNotExist implements JoinTournamentResponse {
        String commandId;
        String tournamentId;
    }
    @Value
    class TournamentIsFull implements JoinTournamentResponse {
        String commandId;
        String tournamentId;
    }
    @Value
    class TournamentJoined implements JoinTournamentResponse, Event {
        String commandId;
        String tournamentId;
        String playerId;

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
