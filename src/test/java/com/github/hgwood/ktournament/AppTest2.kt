package com.github.hgwood.ktournament

import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.Unirest
import org.junit.BeforeClass
import org.junit.Test
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import java.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.assertEquals


class AppTest2 {

  val objectMapper = ObjectMapper()

  @BeforeTest
  fun beforeClass() {
    Unirest.setObjectMapper(object : com.mashape.unirest.http.ObjectMapper {
      private val jacksonObjectMapper = com.fasterxml.jackson.databind.ObjectMapper()

      override fun <T> readValue(value: String, valueType: Class<T>): T {
        try {
          return jacksonObjectMapper.readValue(value, valueType)
        } catch (e: IOException) {
          throw RuntimeException(e)
        }

      }

      override fun writeValue(value: Any): String {
        try {
          return jacksonObjectMapper.writeValueAsString(value)
        } catch (e: JsonProcessingException) {
          throw RuntimeException(e)
        }

      }
    })
  }

  @Test
  fun test() {
    val event = JsonNodeFactory.instance.objectNode()
    event.putArray("records")
      .addObject()


    val response = Unirest.post("http://192.168.99.100:8082/topics/events")
      .header("Content-Type", "application/vnd.kafka.binary.v2+json")
      .header("Accept", "application/vnd.kafka.v2+json, application/vnd.kafka+json, application/json")
      .body(event)
      .asJson()
    assertEquals(200, response.status)
  }
}
