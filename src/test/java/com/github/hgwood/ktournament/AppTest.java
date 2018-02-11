package com.github.hgwood.ktournament;

import io.vavr.collection.HashMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.UUID;

public class AppTest {

    @Test public void test() throws Exception {
        try (
            KafkaProducer<UUID, Command> producer = new KafkaProducer<>(
                HashMap.<String, Object>of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092").toJavaMap(),
                KTournamentJoinLogic.uuidSerde.serializer(),
                KTournamentJoinLogic.commandSerde.serializer()
            )
        ){
            UUID entityId = UUID.randomUUID();
            int maxPlayers = 4;
            producer.send(
                new ProducerRecord<>(
                    "tournament-joining-commands",
                    entityId,
                    new TournamentJoinLogic.OpenTournamentToPlayers(UUID.randomUUID(), maxPlayers)
                )
            ).get();
            for (int i = 0; i < maxPlayers + 1; i++) {
                producer.send(
                    new ProducerRecord<>(
                        "tournament-joining-commands",
                        entityId,
                        new TournamentJoinLogic.JoinTournamentCommand(UUID.randomUUID(), UUID.randomUUID())
                    )
                ).get();
            }
        }

    }

}
