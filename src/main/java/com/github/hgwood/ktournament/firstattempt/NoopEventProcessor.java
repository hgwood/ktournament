package com.github.hgwood.ktournament.firstattempt;

import com.github.hgwood.ktournament.secondattempt.PlayerJoinedTournament;

public class NoopEventProcessor implements EventProcessor {

    public void process(TournamentOpened event) {

    }

    public void process(PlayerRequestsJoin event) {

    }

    public void process(TournamentNotFound tournamentNotFound) {

    }

    @Override
    public void process(PlayerJoinedTournament event) {

    }
}
