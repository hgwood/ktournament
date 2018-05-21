package com.github.hgwood.ktournament.framework;

import java.util.List;

import static java.util.Collections.emptyList;

public interface Command<T extends State> {
    default List<Event<T>> decide(T state) {
        return emptyList();
    }
}
