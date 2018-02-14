package com.github.hgwood.ktournament.framework;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;

import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class EventSourcingTopology<S extends State> {
    private final StoreBuilder<KeyValueStore<UUID, StateEnvelope<S>>> store;
    private final StoreBuilder<KeyValueStore<UUID, EventEnvelope<S>>> buffer;
    private final Serde<UUID> entityIdSerde;
    private final String commandTopicName;
    private final Serde<CommandEnvelope<S>> commandSerde;
    private final String eventTopicName;
    private final Serde<EventEnvelope<S>> eventSerde;

    public void build(StreamsBuilder builder) {
        builder.addStateStore(this.store);
        builder.addStateStore(this.buffer);

        ProcessorSupplier<UUID, EventEnvelope<S>> eventProcessor =
            () -> new Deduplicate<>(
                new Evolve<>(),
                buffer.name(),
                (entityId, event) -> event.getId()
            );

        KStream<UUID, CommandEnvelope<S>> commands =
            builder.stream(
                commandTopicName,
                Consumed.with(entityIdSerde, commandSerde)
            );

        KStream<UUID, EventEnvelope<S>> asyncEvents =
            builder.stream(
                eventTopicName,
                Consumed.with(entityIdSerde, eventSerde)
            );

        KStream<UUID, EventEnvelope<S>> syncEvents =
            commands.transform(() -> new Decide<>(), store.name());
        syncEvents.merge(asyncEvents).process(eventProcessor, store.name());
        syncEvents.to(eventTopicName, Produced.with(entityIdSerde, eventSerde));
    }

    public void connect(
        KStream<UUID, CommandEnvelope<S>> commandStream,
        KStream<UUID, EventEnvelope<S>> eventStream,
        Consumer<KStream<UUID, EventEnvelope<S>>> eventSink
    ) {
        ProcessorSupplier<UUID, EventEnvelope<S>> eventProcessor =
            () -> new Deduplicate<>(
                new Evolve<>(),
                buffer.name(),
                (entityId, event) -> event.getId()
            );
        KStream<UUID, EventEnvelope<S>> syncEvents =
            commandStream.transform(() -> new Decide<>(), store.name());
        syncEvents.merge(eventStream).process(eventProcessor, store.name());
        eventSink.accept(syncEvents);
    }
}
