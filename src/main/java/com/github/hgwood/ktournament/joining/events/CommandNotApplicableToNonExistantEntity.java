package com.github.hgwood.ktournament.joining.events;

import com.github.hgwood.ktournament.joining.TournamentJoiningEvent;
import lombok.Value;

@Value
public class CommandNotApplicableToNonExistantEntity implements TournamentJoiningEvent {
}
