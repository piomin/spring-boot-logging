package pl.piomin.logging.reactive.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import pl.piomin.logging.reactive.interceptor.RequestLoggingInterceptor;
import pl.piomin.logging.reactive.interceptor.ResponseLoggingInterceptor;
import pl.piomin.logging.reactive.util.UniqueIDGenerator;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;

public class ReactiveSpringLoggingFilter implements WebFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveSpringLoggingFilter.class);
    private UniqueIDGenerator generator;
    private String ignorePatterns;
    private boolean logHeaders;
    private boolean useContentLength;
    private String requestId;

    public ReactiveSpringLoggingFilter(UniqueIDGenerator generator, String ignorePatterns, boolean logHeaders, boolean useContentLength) {
        this.generator = generator;
        this.ignorePatterns = ignorePatterns;
        this.logHeaders = logHeaders;
        this.useContentLength = useContentLength;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (ignorePatterns != null && exchange.getRequest().getURI().getPath().matches(ignorePatterns)) {
            return chain.filter(exchange);
        } else {
            generator.generateAndSetMDC(exchange.getRequest());
            final long startTime = System.currentTimeMillis();
            List<String> header = exchange.getRequest().getHeaders().get("Content-Length");

            List<String> requestIdList = exchange.getRequest().getHeaders().get("X-Request-Id");
            if (requestIdList == null || requestIdList.size() == 0) {
                requestId = UUID.randomUUID().toString();
            } else {
                requestId = requestIdList.get(0);
            }
            exchange.getResponse().getHeaders().set("X-Request-Id", requestId);

            if (useContentLength && (header == null || header.get(0).equals("0"))) {
                if (logHeaders)
                    LOGGER.info("Request: id={}, method={}, uri={}, headers={}, audit={}", requestId, exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(), exchange.getRequest().getHeaders(), value("audit", true));
                else
                    LOGGER.info("Request: id={}, method={}, uri={}, audit={}", requestId, exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(), value("audit", true));
            }
            ServerWebExchangeDecorator exchangeDecorator = new ServerWebExchangeDecorator(exchange) {
                @Override
                public ServerHttpRequest getRequest() {
                    return new RequestLoggingInterceptor(super.getRequest(), logHeaders, requestId);
                }

                @Override
                public ServerHttpResponse getResponse() {
                    return new ResponseLoggingInterceptor(super.getResponse(), startTime, logHeaders, requestId);
                }
            };
            return chain.filter(exchangeDecorator)
                    .doOnSuccess(aVoid -> {
                        logResponse(startTime, exchangeDecorator.getResponse(), exchangeDecorator.getResponse().getStatusCode().value(), requestId);
                    })
                    .doOnError(throwable -> {
                        logResponse(startTime, exchangeDecorator.getResponse(), 500, requestId);
                    });
        }
    }

    private void logResponse(long startTime, ServerHttpResponse response, int overriddenStatus, String requestId) {
        final long duration = System.currentTimeMillis() - startTime;
        List<String> header = response.getHeaders().get("Content-Length");
        if (useContentLength && (header == null || header.get(0).equals("0"))) {
            if (logHeaders)
                LOGGER.info("Response({} ms): id={}, status={}, headers={}, audit={}", value("X-Response-Time", duration), requestId,
                        value("X-Response-Status", overriddenStatus), response.getHeaders(), value("audit", true));
            else
                LOGGER.info("Response({} ms): id={}, status={}, audit={}", value("X-Response-Time", duration), requestId,
                        value("X-Response-Status", overriddenStatus), value("audit", true));
        }
    }

}
