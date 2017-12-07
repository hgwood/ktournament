package com.github.hgwood.ktournament;

import com.github.hgwood.ktournament.firstattempt.OpenTournament;
import com.github.hgwood.ktournament.firstattempt.TournamentOpened;

public interface Evolutions {
    default void evolve(OpenTournament event) {

    }

    default void evolve(TournamentOpened event) {

    }

    default void evolve(PlayerJoinedTournament event) {

    }
}
