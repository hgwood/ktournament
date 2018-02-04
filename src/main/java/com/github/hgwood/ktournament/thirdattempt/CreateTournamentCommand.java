package com.github.hgwood.ktournament.thirdattempt;

import lombok.Value;

@Value
public class CreateTournamentCommand implements Command {
    String id;
    String name;
    int maxPlayers;
}
