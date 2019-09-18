package pl.piomin.logging.filter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import pl.piomin.logging.util.UniqueIDGenerator;
import pl.piomin.logging.wrapper.SpringRequestWrapper;
import pl.piomin.logging.wrapper.SpringResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.value;

public class SpringLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringLoggingFilter.class);
    private UniqueIDGenerator generator;

    @Value("${logging.logstash.ignorePatterns:#{null}}")
    Optional<String> ignorePatterns;
    @Value("${logging.logstash.includeHeaders:false}")
    boolean logHeaders;
    @Autowired
    ApplicationContext context;

    public SpringLoggingFilter(UniqueIDGenerator generator) {
        this.generator = generator;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (ignorePatterns.isPresent() && request.getRequestURI().matches(ignorePatterns.get())) {
            chain.doFilter(request, response);
        } else {
            generator.generateAndSetMDC(request);
            try {
                getHandlerMethod(request);
            } catch (Exception e) {
                LOGGER.trace("Cannot get handler method");
            }
            final long startTime = System.currentTimeMillis();
            final SpringRequestWrapper wrappedRequest = new SpringRequestWrapper(request);
            if (logHeaders)
                LOGGER.info("Request: method={}, uri={}, payload={}, headers={}, audit={}", wrappedRequest.getMethod(),
                        wrappedRequest.getRequestURI(), IOUtils.toString(wrappedRequest.getInputStream(),
                        wrappedRequest.getCharacterEncoding()), wrappedRequest.getAllHeaders(), true);
            else
                LOGGER.info("Request: method={}, uri={}, payload={}, audit={}", wrappedRequest.getMethod(),
                        wrappedRequest.getRequestURI(), IOUtils.toString(wrappedRequest.getInputStream(),
                        wrappedRequest.getCharacterEncoding()), true);
            final SpringResponseWrapper wrappedResponse = new SpringResponseWrapper(response);
            wrappedResponse.setHeader("X-Request-ID", MDC.get("X-Request-ID"));
            wrappedResponse.setHeader("X-Correlation-ID", MDC.get("X-Correlation-ID"));

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
        if (logHeaders)
            LOGGER.info("Response({} ms): status={}, payload={}, headers={}, audit={}", value("X-Response-Time", duration),
                    value("X-Response-Status", overriddenStatus), IOUtils.toString(wrappedResponse.getContentAsByteArray(),
                            wrappedResponse.getCharacterEncoding()), wrappedResponse.getAllHeaders(), true);
        else
            LOGGER.info("Response({} ms): status={}, payload={}, audit={}", value("X-Response-Time", duration),
                    value("X-Response-Status", overriddenStatus),
                    IOUtils.toString(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding()), true);
    }

    private void getHandlerMethod(HttpServletRequest request) throws Exception {
        RequestMappingHandlerMapping mappings1 = (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mappings1.getHandlerMethods();
        HandlerExecutionChain handler = mappings1.getHandler(request);
        if (Objects.nonNull(handler)) {
            HandlerMethod handler1 = (HandlerMethod) handler.getHandler();
            MDC.put("X-Operation-Name", handler1.getBeanType().getSimpleName() + "." + handler1.getMethod().getName());
        }
    }

}
