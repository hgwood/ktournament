package com.github.hgwood.ktournament;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreSupplier;
import org.apache.kafka.streams.state.Stores;

import java.util.Properties;
import java.util.UUID;

public class KTournamentJoinLogic {

    private static TournamentJoinLogic logic = new TournamentJoinLogic();

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
        StoreSupplier store = Stores.persistentKeyValueStore("myValueTransformState");
        KStream<UUID, Command> commands = builder.stream("commands");
        KStream<UUID, Event> eventsProduced = commands.transform(Decide::new, "myValueTransformState");
        KStream<UUID, Event> eventsFromMaster = builder.stream("tournament-creator-events");
        eventsFromMaster.merge(eventsProduced).process(Evolve::new, "myValueTransformState");
        eventsProduced.to("tournament-join-events");
    }

    public static class Decide implements Transformer<UUID, Command, KeyValue<UUID, Event>> {
        private ProcessorContext context;
        private KeyValueStore<UUID, TournamentJoinLogic.State> store;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.store = (KeyValueStore<UUID, TournamentJoinLogic.State>) context.getStateStore("myValueTransformState");
        }

        @Override
        public KeyValue<UUID, Event> transform(UUID key, Command value) {
            TournamentJoinLogic.State state = store.get(key);
            logic.decide(state, value)
                .map(domainEvent -> new Event(key, domainEvent))
                .forEach(event -> this.context.forward(UUID.randomUUID(), event));
            return null;
        }

        @Override
        public KeyValue<UUID, Event> punctuate(long timestamp) {
            return null;
        }

        @Override
        public void close() {

        }
    }

    public static class Evolve implements Processor<UUID, Event> {
        private ProcessorContext context;
        private KeyValueStore<UUID, TournamentJoinLogic.State> store;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.store = (KeyValueStore<UUID, TournamentJoinLogic.State>) context.getStateStore("myValueTransformState");
        }

        @Override
        public void process(UUID key, Event value) {
            TournamentJoinLogic.State state = store.get(key);
            TournamentJoinLogic.State newState = logic.evolve(state, value.getPayload());
            store.put(key, newState);
        }

        @Override
        public void punctuate(long timestamp) {

        }

        @Override
        public void close() {

        }
    }

}
