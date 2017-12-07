package com.github.hgwood.ktournament.firstattempt;

import java.util.List;

public interface CommandProcessor {
    List<? extends Event> process(OpenTournament command);
    List<? extends Event> process(JoinTournament command);
}
