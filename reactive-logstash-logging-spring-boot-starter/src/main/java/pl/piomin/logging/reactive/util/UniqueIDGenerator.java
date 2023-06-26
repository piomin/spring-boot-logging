package pl.piomin.logging.reactive.util;

import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.UUID;

public class UniqueIDGenerator {

    private final String requestIdHeaderName;
    private final String correlationIdHeaderName;

    public UniqueIDGenerator(String requestIdHeaderName, String correlationIdHeaderName) {
        this.requestIdHeaderName = requestIdHeaderName;
        this.correlationIdHeaderName = correlationIdHeaderName;
    }

    public void generateAndSetMDC(ServerHttpRequest request) {
        List<String> requestIds = request.getHeaders().get(requestIdHeaderName);
        if (requestIds == null)
            MDC.put(requestIdHeaderName, UUID.randomUUID().toString());
        else
            MDC.put(requestIdHeaderName, requestIds.get(0));

        List<String> correlationIds = request.getHeaders().get(correlationIdHeaderName);
        if (correlationIds == null)
            MDC.put(correlationIdHeaderName, UUID.randomUUID().toString());
        else
            MDC.put(correlationIdHeaderName, correlationIds.get(0));
    }

}
