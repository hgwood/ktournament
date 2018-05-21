package com.github.hgwood.ktournament.joining.commands

import com.github.hgwood.ktournament.framework.Event
import com.github.hgwood.ktournament.joining.TournamentJoiningCommand
import com.github.hgwood.ktournament.joining.TournamentJoiningState
import com.github.hgwood.ktournament.joining.events.*
import java.util.*


data class JoinTournament(private val playerId: UUID) : TournamentJoiningCommand {
  override fun decide(state: TournamentJoiningState?): List<Event<TournamentJoiningState>> = when {
    state == null -> listOf(CommandNotApplicableToNonExistantEntity())
    !state.acceptingPlayers -> listOf(TournamentIsNotAcceptingPlayers())
    state.playersJoined >= state.maxPlayers -> listOf(TournamentIsFull(this.playerId, state.playersJoined, state.maxPlayers))
    else -> {
      val newPlayersJoined = state.playersJoined + 1
      listOf(
        TournamentJoinedByPlayer(
          this.playerId,
          state.playersJoined,
          newPlayersJoined,
          newPlayersJoined == state.maxPlayers
        )
      )
    }
  }
}

data class OpenTournamentToPlayers(private val maxPlayers: Int) : TournamentJoiningCommand {
  override fun decide(state: TournamentJoiningState?): List<Event<TournamentJoiningState>> = when (state) {
      null -> listOf(TournamentCreated(maxPlayers))
      else -> listOf(AttemptToOverwriteAnExistingTournament())
  }
}

class StartTournament : TournamentJoiningCommand {
  companion object {
    const val MINIMUM_PLAYERS_REQUIRED_TO_START = 2
  }

  override fun decide(state: TournamentJoiningState): List<Event<TournamentJoiningState>> = when {
    state.playersJoined < MINIMUM_PLAYERS_REQUIRED_TO_START -> listOf(NotEnoughPlayersInTournamentToStart(state.playersJoined))
    else -> listOf(TournamentStarted())
  }
}
