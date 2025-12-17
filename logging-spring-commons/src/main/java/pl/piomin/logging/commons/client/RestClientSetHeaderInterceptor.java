package pl.piomin.logging.commons.client;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class RestClientSetHeaderInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDC.get("X-Correlation-ID");
        if (correlationId != null) {
            request.getHeaders().add("X-Correlation-ID", correlationId);
        }
        String requestId = MDC.get("X-Request-ID");
        if (requestId != null) {
            request.getHeaders().add("X-Request-ID", requestId);
        }
        return execution.execute(request, body);
    }

}
