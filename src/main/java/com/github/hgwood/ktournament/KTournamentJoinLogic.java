package com.github.hgwood.ktournament;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hgwood.ktournament.support.json.JsonSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.*;
import ratpack.sse.Event;

import java.util.Properties;
import java.util.UUID;

import static java.lang.String.format;

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
        streamsConfiguration.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, DeserExceptionHandler.class);
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
        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore("tournament-joining-store");
        StoreBuilder<KeyValueStore<UUID, StateEnvelope<TournamentJoiningState>>> store = Stores.keyValueStoreBuilder(storeSupplier, uuidSerde, stateSerde);
        builder.addStateStore(store);

        KeyValueBytesStoreSupplier bufferSupplier = Stores.persistentKeyValueStore("tournament-joining-sync-buffer");
        StoreBuilder<KeyValueStore<UUID, EventEnvelope<TournamentJoiningState>>> buffer = Stores.keyValueStoreBuilder(bufferSupplier, uuidSerde, eventSerde);
        builder.addStateStore(buffer);

        KStream<UUID, CommandEnvelope<TournamentJoiningState>> commands = builder.stream("tournament-joining-commands", Consumed.with(uuidSerde, commandSerde));
        KStream<UUID, EventEnvelope<TournamentJoiningState>> syncEvents = commands.transform(Decide::new, store.name());
        KStream<UUID, EventEnvelope<TournamentJoiningState>> asyncEvents = builder.stream("tournament-joining-events", Consumed.with(uuidSerde, eventSerde));
        syncEvents.merge(asyncEvents).process(Evolve::new, store.name(), buffer.name());
        // OK but then all events are processed twice, need deduplication
        syncEvents.to("tournament-joining-events", Produced.with(uuidSerde, eventSerde));
    }

    public static class Decide implements Transformer<UUID, CommandEnvelope<TournamentJoiningState>, KeyValue<UUID, EventEnvelope<TournamentJoiningState>>> {
        private ProcessorContext context;
        private KeyValueStore<UUID, StateEnvelope<TournamentJoiningState>> store;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.store = (KeyValueStore<UUID, StateEnvelope<TournamentJoiningState>>) context.getStateStore("tournament-joining-store");
        }

        @Override
        public KeyValue<UUID, EventEnvelope<TournamentJoiningState>> transform(UUID key, CommandEnvelope<TournamentJoiningState> value) {
            System.out.println(format("entity %s: applying command %s", key, value.getId()));
            // key cannot be null here otherwise it would not land in the same partition as the next commands for the
            // same aggregate ; that means aggregate id is assigned upstream
            StateEnvelope<TournamentJoiningState> state = store.get(key);
            // payload might be null here! every command has to check for it
            value.getPayload().decide(state == null ? null : state.getPayload())
                .map(event -> new EventEnvelope<TournamentJoiningState>(UUID.randomUUID(), value.getId(), event))
                .forEach(event -> this.context.forward(key, event));
            // produce a command report with: entity state id, produced event ids => includes this in events
            return null;
        }

        @Override
        public KeyValue<UUID, EventEnvelope<TournamentJoiningState>> punctuate(long timestamp) {
            return null;
        }

        @Override
        public void close() {

        }
    }

    public static class Evolve implements Processor<UUID, EventEnvelope<TournamentJoiningState>> {
        private ProcessorContext context;
        private KeyValueStore<UUID, StateEnvelope<TournamentJoiningState>> store;
        private KeyValueStore<UUID, EventEnvelope<TournamentJoiningState>> buffer;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.store = (KeyValueStore<UUID, StateEnvelope<TournamentJoiningState>>) context.getStateStore("tournament-joining-store");
            this.buffer = (KeyValueStore<UUID, EventEnvelope<TournamentJoiningState>>) context.getStateStore("tournament-joining-sync-buffer");
        }

        @Override
        public void process(UUID key, EventEnvelope<TournamentJoiningState> value) {
            if (this.buffer.get(value.getId()) != null) {
                this.buffer.delete(value.getId());
                return;
            }
            System.out.println(format("entity %s: applying event %s from own processing", key, value.getId()));
            StateEnvelope<TournamentJoiningState> envelope = store.get(key);
            if (envelope == null) {
                envelope = StateEnvelope.zero();
            }
            TournamentJoiningState state = envelope.getPayload();
            // state might be null, event have to handle it
            TournamentJoiningState newState = value.getPayload().evolve(state);
            if (state != newState) store.put(key, envelope.next(value.getId(), newState));
            // produced event report with: previous version, new version, entity id => include this in produced states
            this.buffer.put(value.getId(), value);
        }

        @Override
        public void punctuate(long timestamp) {
            // may use this to take snapshots of states
        }

        @Override
        public void close() {

        }
    }

}
