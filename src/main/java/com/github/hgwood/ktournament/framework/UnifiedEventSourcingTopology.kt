package com.github.hgwood.ktournament.framework

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.Consumed
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.StoreBuilder
import java.util.*
import java.util.function.Consumer

class EventSourcingTopology<S : State>(
  private val objectMapper: ObjectMapper,
  private val store: StoreBuilder<KeyValueStore<UUID, StateEnvelope<S>>>,
  private val buffer: StoreBuilder<KeyValueStore<UUID, EventEnvelope<S>>>,
  private val entityIdSerde: Serde<UUID>,
  private val commandTopicName: String,
  private val commandSerde: Serde<CommandEnvelope<S>>,
  private val eventTopicName: String,
  private val eventSerde: Serde<EventEnvelope<S>>
) {


  fun build(builder: StreamsBuilder) {
    builder.addStateStore(this.store)
    builder.addStateStore(this.buffer)

    val commands = builder.stream(
      commandTopicName,
      Consumed.with(Serdes.String(), Serdes.String())
    )
      .mapValues { value ->
        try {
          objectMapper.readValue<Command<S>>(value, object : TypeReference<Command<S>>() {})
        } catch (exception: Exception) {
          null
        }
      }
      .branch(
        { key, value -> value != null },
        { key: String, value: String -> objectMapper.readValue<Command<S>>(value, Event.class) }
      )

    val asyncEvents = builder.stream(
      eventTopicName,
      Consumed.with(entityIdSerde, eventSerde)
    )

    connect(commands, asyncEvents, { events -> events.to(eventTopicName, Produced.with(entityIdSerde, eventSerde)) })
  }

  private fun isCommand<K, V>(K key, V value): Boolean {
    return try {
      objectMapper.readValue<Command<S>>(value, object : TypeReference<Command<S>>() {})
      true
    } catch (exception: Exception) {
      false
    }
  }

  private fun connect(
    commandStream: KStream<UUID, CommandEnvelope<S>>,
    eventStream: KStream<UUID, EventEnvelope<S>>,
    eventSink: Consumer<KStream<UUID, EventEnvelope<S>>>
  ) {
    val eventProcessor = {
      Deduplicate(
        Evolve<S>(),
        buffer.name()
      ) { _, event -> event.id }
    }
    val syncEvents = commandStream.transform<UUID, EventEnvelope<S>>({ Decide<S>() }, store.name())
    syncEvents.merge(eventStream).process(eventProcessor, store.name())
    eventSink.accept(syncEvents)
  }
}
