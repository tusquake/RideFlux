package com.ridesharing.rideservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RIDE_EVENTS_EXCHANGE = "ride.events";
    public static final String RIDE_BOOKED_QUEUE = "ride.booked.queue";
    public static final String RIDE_COMPLETED_QUEUE = "ride.completed.queue";
    public static final String RIDE_CANCELLED_QUEUE = "ride.cancelled.queue";

    public static final String RIDE_BOOKED_KEY = "ride.booked";
    public static final String RIDE_COMPLETED_KEY = "ride.completed";
    public static final String RIDE_CANCELLED_KEY = "ride.cancelled";

    @Bean
    public TopicExchange rideEventsExchange() {
        return new TopicExchange(RIDE_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue rideBookedQueue() {
        return QueueBuilder.durable(RIDE_BOOKED_QUEUE).build();
    }

    @Bean
    public Queue rideCompletedQueue() {
        return QueueBuilder.durable(RIDE_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue rideCancelledQueue() {
        return QueueBuilder.durable(RIDE_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding rideBookedBinding(Queue rideBookedQueue, TopicExchange rideEventsExchange) {
        return BindingBuilder.bind(rideBookedQueue).to(rideEventsExchange).with(RIDE_BOOKED_KEY);
    }

    @Bean
    public Binding rideCompletedBinding(Queue rideCompletedQueue, TopicExchange rideEventsExchange) {
        return BindingBuilder.bind(rideCompletedQueue).to(rideEventsExchange).with(RIDE_COMPLETED_KEY);
    }

    @Bean
    public Binding rideCancelledBinding(Queue rideCancelledQueue, TopicExchange rideEventsExchange) {
        return BindingBuilder.bind(rideCancelledQueue).to(rideEventsExchange).with(RIDE_CANCELLED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
