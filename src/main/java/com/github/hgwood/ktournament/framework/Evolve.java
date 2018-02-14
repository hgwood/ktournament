package com.github.hgwood.ktournament.framework;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.UUID;

import static java.lang.String.format;

public class Evolve<T extends State> implements Processor<UUID, EventEnvelope<T>> {
    private ProcessorContext context;
    private KeyValueStore<UUID, StateEnvelope<T>> store;
    //private KeyValueStore<UUID, EventEnvelope<T>> buffer;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.store = (KeyValueStore<UUID, StateEnvelope<T>>) context.getStateStore("tournament-joining-store");
        //this.buffer = (KeyValueStore<UUID, EventEnvelope<T>>) context.getStateStore("tournament-joining-sync-buffer");
    }

    @Override
    public void process(UUID entityId, EventEnvelope<T> event) {
        /* (this.buffer.get(event.getId()) != null) {
            this.buffer.delete(event.getId());
            return;
        }*/
        System.out.println(format("entity %s: applying event %s from own processing", entityId, event.getId()));
        StateEnvelope<T> envelope = this.store.get(entityId);
        if (envelope == null) {
            envelope = StateEnvelope.zero(entityId);
        }
        T state = envelope.getPayload();
        // state might be null, event have to handle it
        T newState = event.getPayload().evolve(state);
        if (state != newState) this.store.put(entityId, envelope.next(event.getId(), newState));
        // produced event report with: previous version, new version, entity id => include this in produced states
        //this.buffer.put(event.getId(), event);
    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }
}
