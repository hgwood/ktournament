package com.github.hgwood.ktournament2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.github.hgwood.ktournament.framework.StateEnvelope
import com.github.hgwood.ktournament.support.json.JsonSerde
import com.github.hgwood.ktournament2.data.TournamentJoiningState
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.state.Stores.keyValueStoreBuilder
import org.apache.kafka.streams.state.Stores.persistentKeyValueStore
import java.util.*

const val commandTopic = "ktournament.joining.commands"
const val ackTopic = "ktournament.joining.acks"
const val patchTopic = "ktournament.joining.patches"

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
      persistentKeyValueStore("ktournament.joining.store"),
      Serdes.String(),
      stateSerde
    )
  val buffer =
    keyValueStoreBuilder(
      persistentKeyValueStore("ktournament.joining.buffer"),
      Serdes.String(),
      patchSerde
    )
  
}
