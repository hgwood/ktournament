package com.github.hgwood.ktournament.firstattempt;

import java.util.Collections;
import java.util.List;

public class NoopCommandProcessor implements CommandProcessor {

    public List<? extends Event> process(OpenTournament command) {
        return Collections.emptyList();
    }

    public List<? extends Event> process(JoinTournament command) {
        return Collections.emptyList();
    }
}
