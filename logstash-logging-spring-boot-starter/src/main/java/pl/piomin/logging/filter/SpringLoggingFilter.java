package pl.piomin.logging.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import pl.piomin.logging.builder.LoggingPrinter;
import pl.piomin.logging.util.UniqueIDGenerator;
import pl.piomin.logging.commons.wrapper.SpringRequestWrapper;
import pl.piomin.logging.commons.wrapper.SpringResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;


public class SpringLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String OPERATION_NAME_MDC_KEY = "X-Operation-Name";

    private final UniqueIDGenerator generator;
    private final String ignorePatterns;
    private final ApplicationContext context;
    private final LoggingPrinter loggingPrinter;

    public SpringLoggingFilter(UniqueIDGenerator generator, String ignorePatterns, boolean logHeaders, boolean ignorePayload, ApplicationContext context) {
        this.generator = generator;
        this.ignorePatterns = ignorePatterns;
        this.context = context;
        this.loggingPrinter = new LoggingPrinter.Builder()
                .showLogHeader(logHeaders)
                .ignorePayload(ignorePayload)
                .build(LOGGER);
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (ignorePatterns != null && request.getRequestURI().matches(ignorePatterns)) {
            chain.doFilter(request, response);
        } else {
            generator.generateAndSetMDC(request);
            try {
                getHandlerMethod(request);
            } catch (IllegalStateException | IllegalArgumentException e) {
                LOGGER.debug("Cannot get handler method: {}", e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            final long startTime = System.currentTimeMillis();
            final SpringRequestWrapper wrappedRequest = new SpringRequestWrapper(request);
            loggingPrinter.printRequest(wrappedRequest);
            final SpringResponseWrapper wrappedResponse = new SpringResponseWrapper(response);
            wrappedResponse.setHeader(REQUEST_ID_HEADER, MDC.get(REQUEST_ID_HEADER));
            wrappedResponse.setHeader(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));

            try {
                chain.doFilter(wrappedRequest, wrappedResponse);
            } catch (Exception e) {
                logResponse(startTime, wrappedResponse, 500);
                throw e;
            }
            logResponse(startTime, wrappedResponse, wrappedResponse.getStatus());
        }
    }

    private void logResponse(long startTime, SpringResponseWrapper wrappedResponse, int overriddenStatus) throws IOException {
        final long duration = System.currentTimeMillis() - startTime;
        wrappedResponse.setCharacterEncoding("UTF-8");
        loggingPrinter.printResponse(wrappedResponse, duration, overriddenStatus);
    }

    private void getHandlerMethod(HttpServletRequest request) throws Exception {
        RequestMappingHandlerMapping handlerMapping = (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
        HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
        if (Objects.nonNull(handlerChain)) {
            HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();
            MDC.put(OPERATION_NAME_MDC_KEY, handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName());
        }
    }

}
