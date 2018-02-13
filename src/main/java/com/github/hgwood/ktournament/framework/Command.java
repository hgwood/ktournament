package com.github.hgwood.ktournament.framework;

import io.vavr.collection.List;

public interface Command<T extends State> {
    default List<Event> decide(T state) {
        return List.empty();
    }
}
