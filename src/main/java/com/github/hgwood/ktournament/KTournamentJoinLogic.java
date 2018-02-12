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

import java.util.Properties;
import java.util.UUID;

import static java.lang.String.format;

public class KTournamentJoinLogic {

    public static ObjectMapper objectMapper = new ObjectMapper();
    public static Serde<UUID> uuidSerde = new JsonSerde<>(objectMapper, new TypeReference<UUID>() {});
    public static Serde<Command> commandSerde = new JsonSerde<>(objectMapper, new TypeReference<Command>() {});
    public static Serde<Event> eventSerde = new JsonSerde<>(objectMapper, new TypeReference<Event>() {});
    public static Serde<StateEnvelope<TournamentJoinLogic.State1>> stateSerde = new JsonSerde<>(objectMapper, new TypeReference<StateEnvelope<TournamentJoinLogic.State1>>() {});

    private static TournamentJoinLogic logic = new TournamentJoinLogic();

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
//        KTable<UUID, StateEnvelope<TournamentJoinLogic.State1>> entities =
//            builder.stream("tournament-joining-events", Consumed.with(uuidSerde, eventSerde))
//                .groupByKey()
//                .aggregate(
//                    StateEnvelope::zero,
//                    (UUID id, Event event, StateEnvelope<TournamentJoinLogic.State1> stateEnvelope) -> {
//                        System.out.println(format("entity %s: applying event %s from topic", id, event.getId()));
//                        return stateEnvelope.next(
//                            event.getId(),
//                            logic.evolve(stateEnvelope.getPayload(), event.getPayload()));
//                    },
//                    Materialized.<UUID, StateEnvelope<TournamentJoinLogic.State1>, KeyValueStore<org.apache.kafka.common.utils.Bytes,byte[]>>as("tournament-joining-store").withKeySerde(uuidSerde).withValueSerde(stateSerde));
//


        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore("tournament-joining-store");
        StoreBuilder<KeyValueStore<UUID, StateEnvelope<TournamentJoinLogic.State1>>> store = Stores.keyValueStoreBuilder(storeSupplier, uuidSerde, stateSerde);
        builder.addStateStore(store);
        KeyValueBytesStoreSupplier bufferSupplier = Stores.persistentKeyValueStore("tournament-joining-sync-buffer");
        StoreBuilder<KeyValueStore<UUID, Event>> buffer = Stores.keyValueStoreBuilder(bufferSupplier, uuidSerde, eventSerde);
        builder.addStateStore(buffer);


        KStream<UUID, Command> commands = builder.stream("tournament-joining-commands", Consumed.with(uuidSerde, commandSerde));
        KStream<UUID, Event> syncEvents = commands.transform(Decide::new, store.name());
        KStream<UUID, Event> asyncEvents = builder.stream("tournament-joining-events", Consumed.with(uuidSerde, eventSerde));
        syncEvents.merge(asyncEvents).process(Evolve::new, store.name(), buffer.name());
        // OK but then all events are processed twice, need deduplication
        syncEvents.to("tournament-joining-events", Produced.with(uuidSerde, eventSerde));
    }

    public static class Decide implements Transformer<UUID, Command, KeyValue<UUID, Event>> {
        private ProcessorContext context;
        private KeyValueStore<UUID, StateEnvelope<TournamentJoinLogic.State1>> store;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.store = (KeyValueStore<UUID, StateEnvelope<TournamentJoinLogic.State1>>) context.getStateStore("tournament-joining-store");
        }

        @Override
        public KeyValue<UUID, Event> transform(UUID key, Command value) {
            System.out.println(format("entity %s: applying command %s", key, value.getId()));
            // key cannot be null here otherwise it would not land in the same partition as the next commands for the
            // same aggregate ; that means aggregate id is assigned upstream
            StateEnvelope<TournamentJoinLogic.State1> state = store.get(key);
            // payload might be null here! every command has to check for it
            logic.decide(state == null ? null : state.getPayload(), value)
                .map(domainEvent -> new Event(UUID.randomUUID(), value.getId(), domainEvent))
                .forEach(event -> this.context.forward(key, event));
            // produce a command report with: entity state id, produced event ids => includes this in events
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
        private KeyValueStore<UUID, StateEnvelope<TournamentJoinLogic.State1>> store;
        private KeyValueStore<UUID, Event> buffer;

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.store = (KeyValueStore<UUID, StateEnvelope<TournamentJoinLogic.State1>>) context.getStateStore("tournament-joining-store");
            this.buffer = (KeyValueStore<UUID, Event>) context.getStateStore("tournament-joining-sync-buffer");
        }

        @Override
        public void process(UUID key, Event value) {
            if (this.buffer.get(value.getId()) != null) {
                this.buffer.delete(value.getId());
                return;
            }
            System.out.println(format("entity %s: applying event %s from own processing", key, value.getId()));
            StateEnvelope<TournamentJoinLogic.State1> envelope = store.get(key);
            if (envelope == null) {
                envelope = StateEnvelope.zero();
            }
            TournamentJoinLogic.State1 state = envelope.getPayload();
            // event handler handles nulls
            //if (state == null)  {
                //context.forward(UUID.randomUUID(), new NonExistantEntityReferencedInEvent(key, value));
                //throw new NullPointerException("event referencing non existant entity");
            //}
            TournamentJoinLogic.State1 newState = logic.evolve(state, value.getPayload());
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
