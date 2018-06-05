package com.github.hgwood.ktournament2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import com.github.hgwood.ktournament.support.json.JsonSerde
import com.github.hgwood.ktournament2.data.PlayerJoinedTournament
import com.github.hgwood.ktournament2.data.TournamentJoiningState
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores.keyValueStoreBuilder
import org.apache.kafka.streams.state.Stores.persistentKeyValueStore

const val commandTopic = "ktournament.joining.commands"
const val ackTopic = "ktournament.joining.acks"
const val patchTopic = "ktournament.joining.patches"
const val storeName = "ktournament.joining.store"
const val bufferStoreName = "ktournament.joining.buffer"

fun main(args: Array<String>) {
  val streamsConfig = StreamsConfig(mapOf(
    BOOTSTRAP_SERVERS_CONFIG to System.getenv("KTOURNAMENT_KAFKA"),
    APPLICATION_ID_CONFIG to "wordcount-lambda-example",
    CLIENT_ID_CONFIG to "wordcount-lambda-example-client",
    AUTO_OFFSET_RESET_CONFIG to "earliest",
    DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass,
    DEFAULT_VALUE_SERDE_CLASS_CONFIG to SpecificAvroSerde::class.java,
    SCHEMA_REGISTRY_URL_CONFIG to System.getenv("KTOURNAMENT_SCHEMA_REGISTRY")
  ))

  val builder = StreamsBuilder()
  buildTopology(builder)
  val streams = KafkaStreams(builder.build(), streamsConfig)
}

fun buildTopology(builder: StreamsBuilder) {
  val stateSerde = SpecificAvroSerde<TournamentJoiningState>().apply { configure(mapOf(SCHEMA_REGISTRY_URL_CONFIG to System.getenv("KTOURNAMENT_SCHEMA_REGISTRY")), false) }
  val patchSerde = JsonSerde(ObjectMapper(), object : TypeReference<JsonPatch>() {})//SpecificAvroSerde<TournamentJoiningState>().apply { configure(mapOf(SCHEMA_REGISTRY_URL_CONFIG to System.getenv("KTOURNAMENT_SCHEMA_REGISTRY")), false) }
  val store =
    keyValueStoreBuilder<String, TournamentJoiningState>(
      persistentKeyValueStore(storeName),
      Serdes.String(),
      stateSerde
    )
  val buffer =
    keyValueStoreBuilder(
      persistentKeyValueStore(bufferStoreName),
      Serdes.String(),
      patchSerde
    )
  val commands = builder.stream<String, SpecificRecord>(commandTopic)

}

fun generatePatches(key: String?, value: SpecificRecord?, store: KeyValueStore<String, TournamentJoiningState>): List<JsonPatch> = when (value) {
  is PlayerJoinedTournament -> {
    val state: TournamentJoiningState? = store.get(value.getTournamentId().toString())
    if (state == null) emptyList()
    else listOf(
      JsonPatch.fromJson(
        JsonNodeFactory.instance.arrayNode()
          .addObject()
          .put("op", "replace")
          .put("path", "/${TournamentJoiningState.FIELD_playersJoined}")
          .put("value", state.getPlayersJoined() + 1)
      )
    )
  }
  else -> emptyList()
    /*listOf(
      JsonPatch.fromJson(
        JsonNodeFactory.instance.arrayNode()
          .addObject()
          .put("op", "add")
          .put("path", "/")
          .set(
            "value",
            objectMapper.valueToTree(TournamentJoiningState()
              .apply {
                setAcceptingPlayers(true)
                setMaxPlayers(8)
                setPlayersJoined(0)
              }
            )
          )
      )
    )*/
  }
}

class Decide<T> : Transformer<String, SpecificRecord, KeyValue<String, JsonPatch>> {
  private lateinit var context: ProcessorContext
  private lateinit var store: KeyValueStore<String, TournamentJoiningState>

  override fun punctuate(timestamp: Long): KeyValue<String, JsonPatch> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun init(context: ProcessorContext) {
    this.context = context
    this.store = context.getStateStore("tournament-joining-store") as KeyValueStore<String, TournamentJoiningState>
  }

  override fun transform(key: String?, value: SpecificRecord?): KeyValue<String, JsonPatch>? {
    generatePatches(key, value, this.store)
      .forEach { this.context.forward<String, JsonPatch>(key, it) }
    // produce a command report with: entity state id, produced event ids => includes this in events
    return null
  }

  override fun close() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
