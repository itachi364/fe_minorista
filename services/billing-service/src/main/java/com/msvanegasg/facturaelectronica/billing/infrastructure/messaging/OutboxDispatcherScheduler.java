package com.msvanegasg.facturaelectronica.billing.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.eventing.OutboxDispatchReport;
import com.msvanegasg.facturaelectronica.eventing.OutboxDispatcherService;

@Component
@ConditionalOnBean(OutboxDispatcherService.class)
public class OutboxDispatcherScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxDispatcherScheduler.class);

    private final OutboxDispatcherService dispatcher;
    private final int batchSize;
    private final int maxAttempts;

    public OutboxDispatcherScheduler(OutboxDispatcherService dispatcher,
            @Value("${eventing.outbox.batch-size:25}") int batchSize,
            @Value("${eventing.outbox.max-attempts:5}") int maxAttempts) {
        this.dispatcher = dispatcher;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(initialDelayString = "${eventing.outbox.initial-delay-ms:5000}",
            fixedDelayString = "${eventing.outbox.fixed-delay-ms:5000}")
    public void dispatchPendingEvents() {
        OutboxDispatchReport report = dispatcher.dispatchPending(batchSize, maxAttempts);
        if (report.attempted() > 0) {
            LOGGER.info("event=outbox_dispatch attempted={} published={} failed={}", report.attempted(),
                    report.published(), report.failed());
        }
    }
}
