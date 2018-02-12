package com.github.hgwood.ktournament;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.UUID;

import static java.lang.String.format;

public class Evolve implements Processor<UUID, EventEnvelope<TournamentJoiningState>> {
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
    public void process(UUID entityId, EventEnvelope<TournamentJoiningState> event) {
        if (this.buffer.get(event.getId()) != null) {
            this.buffer.delete(event.getId());
            return;
        }
        System.out.println(format("entity %s: applying event %s from own processing", entityId, event.getId()));
        StateEnvelope<TournamentJoiningState> envelope = store.get(entityId);
        if (envelope == null) {
            envelope = StateEnvelope.zero(entityId);
        }
        TournamentJoiningState state = envelope.getPayload();
        // state might be null, event have to handle it
        TournamentJoiningState newState = event.getPayload().evolve(state);
        if (state != newState) store.put(entityId, envelope.next(event.getId(), newState));
        // produced event report with: previous version, new version, entity id => include this in produced states
        this.buffer.put(event.getId(), event);
    }

    @Override
    public void punctuate(long timestamp) {
        // may use this to take snapshots of states
    }

    @Override
    public void close() {

    }
}
