package com.github.hgwood.ktournament.joining

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.hgwood.ktournament.framework.Event
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  JsonSubTypes.Type(name = "TOURNAMENT_CREATED", value = TournamentCreated::class),
  JsonSubTypes.Type(name = "TOURNAMENT_JOINED_BY_PLAYERS", value = TournamentJoinedByPlayer::class),
  JsonSubTypes.Type(name = "TOURNAMENT_IS_FULL", value = TournamentIsFull::class),
  JsonSubTypes.Type(name = "ATTEMPT_TO_OVERWRITE_AN_EXISTING_TOURNAMENT", value = AttemptToOverwriteAnExistingTournament::class)
)
interface TournamentJoiningEvent : Event<TournamentJoiningState>

class AttemptToOverwriteAnExistingTournament : TournamentJoiningEvent

class CommandNotApplicableToNonExistentEntity : TournamentJoiningEvent

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
