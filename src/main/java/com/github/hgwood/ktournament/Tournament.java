package com.github.hgwood.ktournament;

import java.util.*;

import static java.util.Arrays.asList;

public class Tournament implements Decisions, Evolutions {
    UUID id;
    int maxPlayersPerTable;
    int maxTables;
    State state;
    List<List<UUID>> tables;
    Map<UUID, Boolean> readiness;

    public List<? extends Event> decide(OpenTournament event) {
        return asList(new TournamentOpened(UUID.randomUUID(), event.getMaxPlayersPerTable(), event.getMaxTables()));
    }

    public List<? extends Event> decide(JoinTournament event) {
        if (this.hasPlayer(event.getPlayer())) {
            return asList(new PlayerRejected(event.getTournament(), event.getPlayer(), "player already in tournament"));
        }
        if (this.isFull()) {
            return asList(new PlayerRejected(event.getTournament(), event.getPlayer(), "tournament is full"));
        }
        int table = this.nextEmptyPlayerSlot();
        return asList(new PlayerJoinedTournament(event.getTournament(), event.getPlayer(), table));
    }

    private int nextEmptyPlayerSlot() {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).size() < maxPlayersPerTable) {
                return i;
            }
        }
        return tables.size(); // FIXME: not convinced that should be the abort value
    }

    public List<? extends Event> decide(TogglePlayerIsReady event) {
        return asList(this.getReadiness(event.getPlayer())
            .map(playerIsReady -> (Event) new PlayerReadinessToggled(event.getTournament(), event.getPlayer(), playerIsReady))
            .orElse(new TogglePlayerIsReadyRejected(event.getTournament(), event.getPlayer(), "player is not in the tournament")));
    }

    private boolean isFull() {
        return tables.size() == maxTables && tables.get(maxTables - 1).size() == maxPlayersPerTable;
    }

    private boolean hasPlayer(UUID player) {
        return readiness.get(player) != null;
    }

    private Optional<Boolean> getReadiness(UUID player) {
        return Optional.ofNullable(readiness.get(player));
    }

    public void evolve(TournamentOpened event) {
        this.id = event.getTournament();
        this.state = State.OPENED;
        this.tables = new ArrayList<>();
        for (int i = 0; i < maxTables; i++) {
            this.tables.add(new ArrayList<>());
        }
        this.readiness = new HashMap<>();
    }

    public void evolve(PlayerJoinedTournament event) {
        assert this.id == event.getTournament();
        this.tables.get(event.getTable()).add(event.getPlayer());
    }

    public void evolve(PlayerReadinessToggled event) {
        assert this.id == event.getTournament();
        this.readiness.put(event.getPlayer(), event.isReady());
    }


    enum State {
        OPENED,
        ON_GOING,
        CLOSED
    }
}
