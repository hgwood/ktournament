package com.github.hgwood.ktournament;

import io.vavr.collection.HashMap;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.UUID;

public class AppTest {

    @Test public void test() throws Exception {
        try (KafkaProducer<UUID, Command> producer = new KafkaProducer<>(HashMap.<String, Object>of(ProducerConfig
            .BOOTSTRAP_SERVERS_CONFIG, "localhost:9092").toJavaMap(), KTournamentJoinLogic.uuidSerde.serializer(), KTournamentJoinLogic.commandSerde.serializer())){
            producer.send(new ProducerRecord<>("tournament-joining-commands", UUID.randomUUID(), new TournamentJoinLogic
                .OpenTournamentToPlayers(4))).get();
        }

    }

}
