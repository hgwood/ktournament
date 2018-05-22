package com.github.hgwood.ktournament;

import com.github.hgwood.ktournament.framework.CommandEnvelope;
import com.github.hgwood.ktournament.joining.TournamentJoiningState;
import com.github.hgwood.ktournament.joining.JoinTournament;
import com.github.hgwood.ktournament.joining.OpenTournamentToPlayers;
import io.vavr.collection.HashMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.UUID;

public class AppTest {

    @Test public void test() throws Exception {
        try (
            KafkaProducer<UUID, CommandEnvelope<TournamentJoiningState>> producer = new KafkaProducer<>(
                HashMap.<String, Object>of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092").toJavaMap(),
                KTournamentJoinLogic.uuidSerde.serializer(),
                KTournamentJoinLogic.commandSerde.serializer()
            )
        ) {
            UUID entityId = UUID.randomUUID();
            int maxPlayers = 4;
            producer.send(
                new ProducerRecord<>(
                    "tournament-joining-commands",
                    entityId,
                    new CommandEnvelope<>(UUID.randomUUID(), new OpenTournamentToPlayers(maxPlayers))
                )
            ).get();
            for (int i = 0; i < maxPlayers + 1; i++) {
                producer.send(
                    new ProducerRecord<>(
                        "tournament-joining-commands",
                        entityId,
                        new CommandEnvelope<>(UUID.randomUUID(), new JoinTournament(UUID.randomUUID()))
                    )
                ).get();
            }
        }

    }

}
