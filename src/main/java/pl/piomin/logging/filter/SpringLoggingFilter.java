package pl.piomin.logging.filter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.piomin.logging.util.UniqueIDGenerator;
import pl.piomin.logging.wrapper.SpringRequestWrapper;
import pl.piomin.logging.wrapper.SpringResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.value;

public class SpringLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringLoggingFilter.class);
    private UniqueIDGenerator generator;

    @Value("${spring.logging.ignorePatterns:#{null}}")
    Optional<String> ignorePatterns;
    @Value("${spring.logging.includeHeaders:false}")
    boolean logHeaders;

    public SpringLoggingFilter(UniqueIDGenerator generator) {
        this.generator = generator;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (ignorePatterns.isPresent() && request.getRequestURI().matches(ignorePatterns.get())) {
            chain.doFilter(request, response);
        } else {
            generator.generateAndSetMDC(request);
            final long startTime = System.currentTimeMillis();
            final SpringRequestWrapper wrappedRequest = new SpringRequestWrapper(request);
            if (logHeaders)
                LOGGER.info("Request: method={}, uri={}, payload={}, headers={}", wrappedRequest.getMethod(),
                        wrappedRequest.getRequestURI(), IOUtils.toString(wrappedRequest.getInputStream(),
                        wrappedRequest.getCharacterEncoding()), wrappedRequest.getAllHeaders());
            else
                LOGGER.info("Request: method={}, uri={}, payload={}", wrappedRequest.getMethod(),
                        wrappedRequest.getRequestURI(), IOUtils.toString(wrappedRequest.getInputStream(),
                        wrappedRequest.getCharacterEncoding()));
            final SpringResponseWrapper wrappedResponse = new SpringResponseWrapper(response);
            wrappedResponse.setHeader("X-Request-ID", MDC.get("X-Request-ID"));
            wrappedResponse.setHeader("X-Correlation-ID", MDC.get("X-Correlation-ID"));
            chain.doFilter(wrappedRequest, wrappedResponse);
            final long duration = System.currentTimeMillis() - startTime;
            if (logHeaders)
                LOGGER.info("Response({} ms): status={}, payload={}, headers={}", value("X-Response-Time", duration),
                        value("X-Response-Status", wrappedResponse.getStatus()), IOUtils.toString(wrappedResponse.getContentAsByteArray(),
                        wrappedResponse.getCharacterEncoding()), wrappedResponse.getAllHeaders());
            else
                LOGGER.info("Response({} ms): status={}, payload={}", value("X-Response-Time", duration),
                        value("X-Response-Status", wrappedResponse.getStatus()),
                        IOUtils.toString(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding()));
        }
    }
}
