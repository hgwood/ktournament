package com.github.hgwood.ktournament.joining.events

import com.github.hgwood.ktournament.joining.TournamentJoiningEvent
import com.github.hgwood.ktournament.joining.TournamentJoiningState
import java.util.*

class AttemptToOverwriteAnExistingTournament : TournamentJoiningEvent

class CommandNotApplicableToNonExistantEntity : TournamentJoiningEvent

class NotEnoughPlayersInTournamentToStart(val playersInTournament: Int) : TournamentJoiningEvent

class TournamentCreated(private val maxPlayers: Int) : TournamentJoiningEvent {
  override fun evolve(state: TournamentJoiningState): TournamentJoiningState =
    TournamentJoiningState(
      acceptingPlayers = true,
      playersJoined = 0,
      maxPlayers = maxPlayers
    )
}

class TournamentIsFull(
  val playerId: UUID,
  val playersJoined: Int,
  val maxPlayers: Int
) : TournamentJoiningEvent

class TournamentIsNotAcceptingPlayers : TournamentJoiningEvent

class TournamentJoinedByPlayer(
  val playerId: UUID,
  val playersBefore: Int,
  val playersAfter: Int,
  val tournamentIsNowFull: Boolean
) : TournamentJoiningEvent {
  override fun evolve(state: TournamentJoiningState): TournamentJoiningState =
    state.copy(playersJoined = this.playersAfter)
}

class TournamentStarted : TournamentJoiningEvent {
  override fun evolve(state: TournamentJoiningState): TournamentJoiningState =
    state.copy(acceptingPlayers = false)
}
