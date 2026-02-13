package com.ridesharing.notificationservice.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RIDE_BOOKED_QUEUE = "ride.booked.queue";
    public static final String RIDE_COMPLETED_QUEUE = "ride.completed.queue";
    public static final String RIDE_CANCELLED_QUEUE = "ride.cancelled.queue";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
