package com.github.hgwood.ktournament.secondattempt;

import com.github.hgwood.ktournament.firstattempt.Event;
import com.github.hgwood.ktournament.firstattempt.OpenTournament;
import com.github.hgwood.ktournament.firstattempt.TournamentOpened;

import java.util.Collections;
import java.util.List;

public interface Decisions {
    default List<? extends Event> decide(OpenTournament event) {
        return Collections.emptyList();
    }

    default List<? extends Event> decide(TournamentOpened event) {
        return Collections.emptyList();
    }
}
