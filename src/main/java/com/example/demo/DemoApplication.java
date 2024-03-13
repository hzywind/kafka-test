package com.example.demo;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public KafkaProducer<String, String> kafkaProducer() {
		Properties props = new Properties();
    	props.put("bootstrap.servers", "localhost:9092");

    	props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    	props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("client.id", "kafka-producer");
		props.put("compression.type", "snappy");
		props.put("acks", "all");
		props.put("retries", 3);
		props.put("max.in.flight.requests.per.connection", 1);
		props.put("enable.idempotence", true);
		props.put("transactional.id", "tx-id");
		return new KafkaProducer<>(props);
	}

	@Bean
	public KafkaConsumer<String, String> kafkaConsumer() {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "test-group");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("auto.offset.reset", "latest");
		props.put("enable.auto.commit", "false");
		props.put("isolation.level", "read_committed");
		return new KafkaConsumer<>(props);
	}

	@Bean
	public CommandLineRunner commandLineRunner(KafkaConsumer<String, String> kafkaConsumer, 
		KafkaProducer<String, String> kafkaProducer) {
		return args -> {
			new Thread(() -> {
				kafkaConsumer.subscribe(Collections.singletonList("test-topic"));
				while (true) {
					ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
					for (ConsumerRecord<String, String> record : records) {
						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>" + record.value());
					} 
					// Manually commit the offset of the last message processed
					kafkaConsumer.commitSync();
				}
			}).start();

			kafkaProducer.initTransactions(); // Initialize transactions
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					kafkaProducer.beginTransaction(); // Start a new transaction
					String message = scanner.nextLine();
					kafkaProducer.send(new ProducerRecord<>("test-topic", "Kafka: " + message));
					kafkaProducer.commitTransaction(); // Commit the transaction
				}
			} catch (Exception e) {
				kafkaProducer.abortTransaction(); // Abort the transaction in case of an exception
			}
		};
	}
}
