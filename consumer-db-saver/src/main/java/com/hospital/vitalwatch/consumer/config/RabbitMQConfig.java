package com.hospital.vitalwatch.consumer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el Consumidor DB Saver
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.alerts}")
    private String alertsQueue;

    /**
     * Define la cola de alertas
     */
    @Bean
    public Queue alertsQueue() {
        return new Queue(alertsQueue, true); // durable = true
    }

    /**
     * Conversor de mensajes JSON
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
