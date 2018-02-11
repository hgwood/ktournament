package com.github.hgwood.ktournament;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

public class App {
    public static void main(String[] args) throws Exception {
        /*RatpackServer.start(server -> server
            .serverConfig(ServerConfig.embedded())
            .registryOf(registry -> {
                ProducerConfig producerConfig = new ProducerConfig(HashMap.of(
                    "bootstrap.servers"
                ))
                registry.add(new KafkaProducer<Void, Object>())
            })
            .handlers(chain -> chain
                .get(":name", ctx -> ctx.render("Hello " + ctx.getPathTokens().get("name") + "!"))
                .post("commands", ctx -> ctx.)
            )
        );*/
    }
}
