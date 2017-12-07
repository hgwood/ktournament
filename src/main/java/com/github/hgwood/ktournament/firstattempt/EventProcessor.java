package com.github.hgwood.ktournament.firstattempt;

import com.github.hgwood.ktournament.PlayerJoinedTournament;
import com.github.hgwood.ktournament.firstattempt.PlayerRequestsJoin;
import com.github.hgwood.ktournament.firstattempt.TournamentNotFound;
import com.github.hgwood.ktournament.firstattempt.TournamentOpened;

public interface EventProcessor {
    void process(TournamentOpened event);

    void process(PlayerRequestsJoin event);

    void process(TournamentNotFound event);

    void process(PlayerJoinedTournament event);
}
