package com.github.hgwood.ktournament.framework;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.function.BiFunction;

@RequiredArgsConstructor
public class Deduplicate<Key, Value, BufferKey> implements Processor<Key, Value> {
    private final Processor<Key, Value> inner;
    private final String bufferStoreName;
    private final BiFunction<Key, Value, BufferKey> storeKeyGetter;
    private KeyValueStore<BufferKey, Value> buffer;

    @Override
    public void init(ProcessorContext context) {
        this.buffer = (KeyValueStore<BufferKey, Value>) context.getStateStore(this.bufferStoreName);
        this.inner.init(context);
    }

    @Override
    public void process(Key key, Value value) {
        BufferKey storeKey = this.storeKeyGetter.apply(key, value);
        if (this.buffer.get(storeKey) != null) {
            this.buffer.delete(storeKey);
            return;
        }
        this.inner.process(key, value);
        this.buffer.put(storeKey, value);
    }

    @Override
    public void punctuate(long timestamp) {
        this.inner.punctuate(timestamp);
    }

    @Override
    public void close() {
        this.inner.close();
    }
}
