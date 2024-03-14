package com.example.demo;

import java.util.Scanner;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@EnableKafka
public class SpringKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringKafkaApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(KafkaTemplate<String, String> kafkaTemplate) {
		return args -> {
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					String message = scanner.nextLine();
					kafkaTemplate.executeInTransaction(ops -> ops.send("test-topic", "Kafka: " + message));
				}
			} 
		};
	}

	@Transactional
	@KafkaListener(topics = "test-topic", groupId = "test-group")
	public void listen(ConsumerRecord<?, ?> record, Acknowledgment acknowledgment) {
		System.out.println("Received Message in Listener: " + record.value());
		acknowledgment.acknowledge();
	}
}
