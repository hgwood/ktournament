package com.github.hgwood.ktournament.framework;

public interface Event<T extends State> {
    default T evolve(T state) {
        return state;
    }
}
