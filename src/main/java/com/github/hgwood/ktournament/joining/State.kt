package com.github.hgwood.ktournament.joining

import com.github.hgwood.ktournament.framework.State

data class TournamentJoiningState(
  val acceptingPlayers: Boolean,
  val playersJoined: Int,
  val maxPlayers: Int
) : State
