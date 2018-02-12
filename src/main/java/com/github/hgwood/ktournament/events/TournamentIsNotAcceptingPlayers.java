package com.github.hgwood.ktournament.events;

import com.github.hgwood.ktournament.Event;
import com.github.hgwood.ktournament.TournamentJoiningState;
import lombok.Value;

@Value
public class TournamentIsNotAcceptingPlayers implements Event<TournamentJoiningState> {

}
