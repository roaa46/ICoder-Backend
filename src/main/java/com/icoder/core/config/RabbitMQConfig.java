package com.icoder.core.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String SUBMISSION_EXCHANGE = "submission.exchange";
    public static final String SUBMISSION_QUEUE = "submission.check.queue";
    public static final String SUBMISSION_DELAY_QUEUE = "submission.delay.queue";
    public static final String SUBMISSION_ROUTING_KEY = "submission.check";

    @Bean
    public DirectExchange submissionExchange() {
        return new DirectExchange(SUBMISSION_EXCHANGE);
    }

    @Bean
    public Queue submissionQueue() {
        return new Queue(SUBMISSION_QUEUE);
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", SUBMISSION_EXCHANGE);
        args.put("x-dead-letter-routing-key", SUBMISSION_ROUTING_KEY);
        args.put("x-message-ttl", 10000);
        return new Queue(SUBMISSION_DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding binding(Queue submissionQueue, DirectExchange submissionExchange) {
        return BindingBuilder.bind(submissionQueue).to(submissionExchange).with(SUBMISSION_ROUTING_KEY);
    }
}