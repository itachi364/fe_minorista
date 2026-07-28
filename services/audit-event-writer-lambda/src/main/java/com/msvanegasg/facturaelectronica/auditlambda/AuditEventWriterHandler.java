package com.msvanegasg.facturaelectronica.auditlambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse.BatchItemFailure;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class AuditEventWriterHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private final EventBridgeSqsEnvelopeParser parser;
    private final ProcessAuditEventRequestedService service;

    public AuditEventWriterHandler() {
        ObjectMapper objectMapper = new ObjectMapper();
        this.parser = new EventBridgeSqsEnvelopeParser(objectMapper);
        this.service = new ProcessAuditEventRequestedService(new AuditEventRequestedMapper(objectMapper),
                new JdbcAuditEventInboxRepository(DatabaseSettings.fromEnvironment()));
    }

    AuditEventWriterHandler(EventBridgeSqsEnvelopeParser parser, ProcessAuditEventRequestedService service) {
        this.parser = Objects.requireNonNull(parser, "parser is required");
        this.service = Objects.requireNonNull(service, "service is required");
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        List<BatchItemFailure> failures = new ArrayList<>();
        if (event == null || event.getRecords() == null) {
            return new SQSBatchResponse(failures);
        }
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            String messageId = message.getMessageId();
            try {
                DomainEventEnvelope envelope = parser.parse(message.getBody());
                AuditEventWriterResult result = service.process(envelope);
                log(context, "event=audit_lambda_processed messageId=" + messageId + " eventId="
                        + envelope.eventId() + " processed=" + result.processed() + " duplicate=" + result.duplicate()
                        + " ignored=" + result.ignored());
            } catch (RuntimeException exception) {
                failures.add(new BatchItemFailure(messageId));
                log(context, "event=audit_lambda_failed messageId=" + messageId + " error="
                        + exception.getClass().getSimpleName() + " message=" + safeMessage(exception));
            }
        }
        return new SQSBatchResponse(failures);
    }

    private void log(Context context, String message) {
        if (context != null && context.getLogger() != null) {
            context.getLogger().log(message + System.lineSeparator());
        }
    }

    private static String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "n/a";
        }
        return message.replace('\n', ' ').replace('\r', ' ');
    }
}
