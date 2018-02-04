package com.github.hgwood.ktournament;

import io.vavr.collection.List;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

public class TournamentJoinLogic {

    public List<DomainEvent> decide(State state, Command command) {
        if (command instanceof JoinTournamentCommand) return decide(state, (JoinTournamentCommand) command);
        if (command instanceof StartTournamentCommand) return decide(state, (StartTournamentCommand) command);
        return List.empty();
    }

    public List<DomainEvent> decide(State state, JoinTournamentCommand command) {
        if (!state.acceptingPlayers) {
            return List.of(new TournamentIsNotAcceptingPlayers());
        } else if (state.playersJoined >= state.maxPlayers) {
            return List.of(new TournamentIsFull(command.playerId, state.playersJoined, state.maxPlayers));
        } else {
            int newPlayersJoined = state.playersJoined + 1;
            return List.of(
                new TournamentJoinedByPlayer(
                    command.playerId,
                    state.playersJoined,
                    newPlayersJoined,
                    newPlayersJoined == state.maxPlayers
                )
            );
        }
    }

    public List<DomainEvent> decide(State state, StartTournamentCommand command) {
        if (state.playersJoined < 2) {
            return List.of(new NotEnoughPlayersInTournamentToStart(state.playersJoined));
        } else {
            return List.of(new TournamentStarted());
        }
    }

    public State evolve(State state, DomainEvent event) {
        if (event instanceof TournamentCreated) return evolve(state, (TournamentCreated) event);
        if (event instanceof TournamentJoinedByPlayer) return evolve(state, (TournamentJoinedByPlayer) event);
        if (event instanceof TournamentStarted) return evolve(state, (TournamentStarted) event);
        return state;
    }

    public State evolve(State state, TournamentCreated event) {
        return State.builder()
            .acceptingPlayers(true)
            .playersJoined(0)
            .maxPlayers(event.maxPlayers)
            .build();
    }

    public State evolve(State state, TournamentJoinedByPlayer event) {
        return state.toBuilder().playersJoined(event.playersAfter).build();
    }

    public State evolve(State state, TournamentStarted event) {
        return state.toBuilder().acceptingPlayers(false).build();
    }

    @Builder(toBuilder = true)
    @Value
    public static class State {
        boolean acceptingPlayers;
        int playersJoined;
        int maxPlayers;
    }

    @Value
    public static class StartTournamentCommand implements Command {

    }

    @Value
    public static class JoinTournamentCommand implements Command {
        UUID playerId;
    }

    @Value
    public static class NotEnoughPlayersInTournamentToStart implements DomainEvent {
        int playersInTournament;
    }

    @Value
    public static class TournamentCreated implements DomainEvent {
        int maxPlayers;
    }

    @Value
    public static class TournamentStarted implements DomainEvent {

    }

    @Value
    public static class TournamentIsNotAcceptingPlayers implements DomainEvent {

    }

    @Value
    public static class TournamentIsFull implements DomainEvent {
        UUID playerId;
        int playersJoined;
        int maxPlayers;
    }

    @Value
    public static class TournamentJoinedByPlayer implements DomainEvent {
        UUID playerId;
        int playersBefore;
        int playersAfter;
        boolean tournamentIsNowFull;
    }
}
