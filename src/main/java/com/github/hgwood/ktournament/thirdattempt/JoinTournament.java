package com.github.hgwood.ktournament.thirdattempt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hgwood.ktournament.support.json.JsonSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
import java.util.UUID;

public class JoinTournament {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Serde<Boolean> booleanSerde = new JsonSerde<>(objectMapper, new TypeReference<Boolean>() {});
    private final static Serde<Command> commandSerde = new JsonSerde<>(objectMapper, new TypeReference<Command>() {});
    private final static Serde<Event> eventSerde = new JsonSerde<>(objectMapper, new TypeReference<Event>() {});
    private final static Serde<JoinTournamentResponse> joinTournamentResponseSerde = new JsonSerde<>(objectMapper, new TypeReference<JoinTournamentResponse>() {});
    private final static Serde<CreateTournamentResponse> createTournamentResponseSerde = new JsonSerde<>(objectMapper, new TypeReference<CreateTournamentResponse>() {});

    public static void main(String[] args) {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-example");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "wordcount-lambda-example-client");
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
        KStream<String, Command> commandStream = builder.stream("commands", Consumed.with(Serdes.String(), commandSerde));
        KStream<String, Event> eventStream = builder.stream("events", Consumed.with(Serdes.String(), eventSerde));

        commandStream
            .filter(((key, command) -> command instanceof CreateTournamentCommand))
            .mapValues(command -> (CreateTournamentCommand) command)
            .mapValues(command -> (CreateTournamentResponse) new CreateTournamentResponse.TournamentCreated(UUID.randomUUID().toString(), command.getName(), command.getMaxPlayers()))
            .to("events", Produced.with(Serdes.String(), createTournamentResponseSerde));

        eventStream
            .filter((key, event) -> event instanceof JoinTournamentResponse.TournamentJoined)
            .mapValues(event -> (JoinTournamentResponse.TournamentJoined) event)
            .groupBy((key, event) -> event.getTournamentId())
            .aggregate(
                () -> 0,
                (key, event, playersJoined) -> playersJoined + 1,
                Materialized.with(Serdes.String(), Serdes.Integer())
            )
            .toStream()
            .to("tournament-players-joined", Produced.with(Serdes.String(), Serdes.Integer()));

        GlobalKTable<String, Boolean> tournamentIsFullTable = builder.globalTable("tournament-is-full", Consumed.with(Serdes.String(), booleanSerde));
        KStream<String, JoinTournamentResponse>[] kStreams = commandStream
            .filter(((key, command) -> command instanceof JoinTournamentCommand))
            .mapValues(command -> (JoinTournamentCommand) command)
            .join(tournamentIsFullTable, (key, command) -> command.getPayload().getTournamentId(), (command, tournamentIsFull) -> {
                if (tournamentIsFull == null) {
                    return new JoinTournamentResponse.TournamentDoesNotExist(command.getId(), command.getPayload().getTournamentId());
                } else if (tournamentIsFull) {
                    return new JoinTournamentResponse.TournamentIsFull(command.getId(), command.getPayload().getTournamentId());
                } else {
                    return new JoinTournamentResponse.TournamentJoined(command.getId(), command.getPayload().getTournamentId(), UUID.randomUUID().toString());
                }
            })
            .branch(
                (key, response) -> response instanceof JoinTournamentResponse.TournamentJoined,
                (key, response) -> true
            );
        kStreams[0].to("events", Produced.with(Serdes.String(), joinTournamentResponseSerde));
        kStreams[1].to("command-errors", Produced.with(Serdes.String(), joinTournamentResponseSerde));
    }
}
