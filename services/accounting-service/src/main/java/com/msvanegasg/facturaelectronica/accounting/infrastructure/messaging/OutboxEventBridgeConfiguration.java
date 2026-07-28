package com.msvanegasg.facturaelectronica.accounting.infrastructure.messaging;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.AwsEventBridgeOutboxEventDeliveryAdapter;
import com.msvanegasg.facturaelectronica.eventing.OutboxDispatcherService;
import com.msvanegasg.facturaelectronica.eventing.OutboxEventDeliveryPort;
import com.msvanegasg.facturaelectronica.eventing.OutboxEventRepositoryPort;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Configuration
public class OutboxEventBridgeConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "eventing.aws.eventbridge.enabled", havingValue = "true")
    EventBridgeClient eventBridgeClient(@Value("${eventing.aws.region:us-east-1}") String region) {
        return EventBridgeClient.builder().region(Region.of(region)).build();
    }

    @Bean
    @ConditionalOnProperty(name = "eventing.aws.eventbridge.enabled", havingValue = "true")
    OutboxEventDeliveryPort eventBridgeOutboxDelivery(EventBridgeClient eventBridgeClient, ObjectMapper objectMapper,
            @Value("${eventing.aws.event-bus-name:facturaelectronica-dev-events}") String eventBusName) {
        return new AwsEventBridgeOutboxEventDeliveryAdapter(eventBridgeClient, objectMapper, eventBusName);
    }

    @Bean
    @ConditionalOnProperty(name = "eventing.aws.eventbridge.enabled", havingValue = "true")
    OutboxDispatcherService outboxDispatcherService(OutboxEventRepositoryPort repository, OutboxEventDeliveryPort delivery) {
        return new OutboxDispatcherService(repository, delivery, Clock.systemUTC());
    }
}
