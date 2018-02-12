package com.github.hgwood.ktournament;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.UUID;

import static java.lang.String.format;

public class Decide implements Transformer<UUID, CommandEnvelope<TournamentJoiningState>, KeyValue<UUID, EventEnvelope<TournamentJoiningState>>> {
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
