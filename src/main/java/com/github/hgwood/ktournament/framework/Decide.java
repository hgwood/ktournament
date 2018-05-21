package com.github.hgwood.ktournament.framework;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.UUID;

import static java.lang.String.format;

public class Decide<T extends State> implements Transformer<UUID, CommandEnvelope<T>, KeyValue<UUID, EventEnvelope<T>>> {
    private ProcessorContext context;
    private KeyValueStore<UUID, StateEnvelope<T>> store;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.store = (KeyValueStore<UUID, StateEnvelope<T>>) context.getStateStore("tournament-joining-store");
    }

    @Override
    public KeyValue<UUID, EventEnvelope<T>> transform(UUID entityId, CommandEnvelope<T> command) {
        System.out.println(format("entity %s: applying command %s", entityId, command.getId()));
        // key cannot be null here otherwise it would not land in the same partition as the next commands for the
        // same aggregate ; that means aggregate id is assigned upstream
        StateEnvelope<T> state = this.store.get(entityId);
        // payload might be null here! every command has to check for it
        command.getPayload().decide(state == null ? null : state.getPayload())
            .stream()
            .map(event -> new EventEnvelope<>(UUID.randomUUID(), command.getId(), event))
            .forEach(event -> this.context.forward(entityId, event));
        // produce a command report with: entity state id, produced event ids => includes this in events
        return null;
    }

    @Override
    public KeyValue<UUID, EventEnvelope<T>> punctuate(long timestamp) {
        return null;
    }

    @Override
    public void close() {

    }
}
