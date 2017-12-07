package com.github.hgwood.ktournament.firstattempt;

import java.util.List;

public interface Command {
    List<Event> accept(CommandProcessor commandProcessor);
}
