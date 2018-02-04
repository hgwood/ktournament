package com.github.hgwood.ktournament.firstattempt;

import com.github.hgwood.ktournament.secondattempt.PlayerJoinedTournament;

public interface EventProcessor {
    void process(TournamentOpened event);

    void process(PlayerRequestsJoin event);

    void process(TournamentNotFound event);

    void process(PlayerJoinedTournament event);
}
