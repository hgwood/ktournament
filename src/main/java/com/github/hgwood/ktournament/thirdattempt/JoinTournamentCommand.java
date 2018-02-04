package com.github.hgwood.ktournament.thirdattempt;

import lombok.Value;

@Value
public class JoinTournamentCommand implements Command {
    String id;
    Payload payload;

    @Value
    public static class Payload {
        String tournamentId;
    }
}
