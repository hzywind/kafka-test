package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

@EnableKafka
@EmbeddedKafka(
    partitions = 1, 
    controlledShutdown = false,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:3333", 
        "port=3333"
})
@SpringBootTest
class DemoApplicationTests {

	@Autowired
    EmbeddedKafkaBroker kafkaEmbeded;

	@Test
	void contextLoads() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>" + kafkaEmbeded.getBrokersAsString());

	}

}
