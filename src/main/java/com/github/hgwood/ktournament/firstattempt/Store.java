package com.github.hgwood.ktournament.firstattempt;

public interface Store<K, V> {

    void put(K key, V value);

    boolean has(K key);
}
