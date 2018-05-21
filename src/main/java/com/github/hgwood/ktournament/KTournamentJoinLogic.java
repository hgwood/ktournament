package com.github.hgwood.ktournament;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hgwood.ktournament.framework.*;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import com.github.hgwood.ktournament.support.json.JsonSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.util.Properties;
import java.util.UUID;

public class KTournamentJoinLogic {

    public static ObjectMapper objectMapper = new ObjectMapper();
    public static Serde<UUID> uuidSerde = new JsonSerde<>(objectMapper, new TypeReference<UUID>() {});
    public static Serde<CommandEnvelope<TournamentJoiningState>> commandSerde = new JsonSerde<>(objectMapper, new TypeReference<CommandEnvelope<TournamentJoiningState>>() {});
    public static Serde<EventEnvelope<TournamentJoiningState>> eventSerde = new JsonSerde<>(objectMapper, new TypeReference<EventEnvelope<TournamentJoiningState>>() {});
    public static Serde<StateEnvelope<TournamentJoiningState>> stateSerde = new JsonSerde<>(objectMapper, new TypeReference<StateEnvelope<TournamentJoiningState>>() {});

    public static void main(String[] args) {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-example");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "wordcount-lambda-example-client");
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final StreamsBuilder builder = new StreamsBuilder();

        buildTopology(builder);

        // Now that we have finished the definition of the processing topology we can actually run
        // it via `start()`.  The Streams application as a whole can be launched just like any
        // normal Java application that has a `main()` method.
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        // Always (and unconditionally) clean local state prior to starting the processing topology.
        // We opt for this unconditional call here because this will make it easier for you to play around with the example
        // when resetting the application for doing a re-run (via the Application Reset Tool,
        // http://docs.confluent.io/current/streams/developer-guide.html#application-reset-tool).
        //
        // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
        // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
        // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
        // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
        // See `ApplicationResetExample.java` for a production-like example.
        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static void buildTopology(StreamsBuilder builder) {
        StoreBuilder<KeyValueStore<UUID, StateEnvelope<TournamentJoiningState>>> store =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("tournament-joining-store"),
                uuidSerde,
                stateSerde
            );

        StoreBuilder<KeyValueStore<UUID, EventEnvelope<TournamentJoiningState>>> buffer =
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("tournament-joining-sync-buffer"),
                uuidSerde,
                eventSerde
            );

        new EventSourcingTopology<>(
            store,
            buffer,
            uuidSerde,
            "tournament-joining-commands",
            commandSerde,
            "tournament-joining-events",
            eventSerde
        ).build(builder);
    }

}
