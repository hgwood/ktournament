package com.github.hgwood.ktournament.joining

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.hgwood.ktournament.framework.Command
import com.github.hgwood.ktournament.framework.Event
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  JsonSubTypes.Type(name = OpenTournamentToPlayers.NAME, value = OpenTournamentToPlayers::class),
  JsonSubTypes.Type(name = JoinTournament.NAME, value = JoinTournament::class),
  JsonSubTypes.Type(name = StartTournament.NAME, value = StartTournament::class)
)
interface TournamentJoiningCommand : Command<TournamentJoiningState>

class JoinTournament(private val playerId: UUID) : TournamentJoiningCommand {
  companion object {
    const val NAME = "JOIN_TOURNAMENT"
  }

  override fun decide(state: TournamentJoiningState?): List<Event<TournamentJoiningState>> = when {
    state == null -> listOf(CommandNotApplicableToNonExistentEntity())
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

class OpenTournamentToPlayers(private val maxPlayers: Int) : TournamentJoiningCommand {
  companion object {
      const val NAME = "OPEN_TOURNAMENT_TO_PLAYERS"
  }

  override fun decide(state: TournamentJoiningState?): List<Event<TournamentJoiningState>> = when (state) {
      null -> listOf(TournamentCreated(maxPlayers))
      else -> listOf(AttemptToOverwriteAnExistingTournament())
  }
}

class StartTournament : TournamentJoiningCommand {
  companion object {
    const val NAME = "START_TOURNAMENT"
    const val MINIMUM_PLAYERS_REQUIRED_TO_START = 2
  }

  override fun decide(state: TournamentJoiningState?): List<Event<TournamentJoiningState>> = when {
    state == null -> listOf(CommandNotApplicableToNonExistentEntity())
    state.playersJoined < MINIMUM_PLAYERS_REQUIRED_TO_START -> listOf(NotEnoughPlayersInTournamentToStart(state.playersJoined))
    else -> listOf(TournamentStarted())
  }
}
