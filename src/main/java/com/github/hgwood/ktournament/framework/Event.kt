package com.github.hgwood.ktournament.framework

import java.util.UUID

interface Event<T : State> {
  fun evolve(state: T?): T? = state
}

class EventEnveloppe<T : State>(val id: UUID, val emittingCommandId: UUID, val payload: Event<T>)
