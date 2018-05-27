package com.github.hgwood.ktournament.framework

import java.util.*

interface Command<T : State> {
  fun decide(state: T?): List<Event<T>> = emptyList()
}

class CommandEnvelope<T : State>(val id: UUID, val payload: Command<T>)
