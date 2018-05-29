package com.github.hgwood.ktournament.http


import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import spark.Spark
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

}

fun server(port: Int = 8080) {
  val kafkaProducer = KafkaProducer<String, String>(
    mapOf(BOOTSTRAP_SERVERS_CONFIG to "localhost:9092"),
    Serdes.String().serializer(),
    Serdes.String().serializer()
  )
  Spark.post("/commands", "application/json") { request, _ ->
    kafkaProducer
      .send(
        ProducerRecord(request.queryParams("key"), request.body())
      )
      .get(2, TimeUnit.SECONDS)
  }
  Spark.awaitInitialization()
  //return Spark.port()
}
