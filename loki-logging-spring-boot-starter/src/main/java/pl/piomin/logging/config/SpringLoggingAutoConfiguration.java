package pl.piomin.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.loki4j.logback.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import pl.piomin.logging.client.RestTemplateSetHeaderInterceptor;
import pl.piomin.logging.filter.SpringLoggingFilter;
import pl.piomin.logging.util.UniqueIDGenerator;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "logging.loki")
public class SpringLoggingAutoConfiguration {

    private static final String LOKI_APPENDER_NAME = "LOKI";

    private String url = "localhost:8500";
    private String ignorePatterns;
    private boolean logHeaders;
    private String trustStoreLocation;
    private String trustStorePassword;
    private String requestIdHeaderName = "X-Request-ID";
    private String correlationIdHeaderName = "X-Correlation-ID";

    @Value("${spring.application.name:-}")
    String name;

    @Bean
    public UniqueIDGenerator generator() {
        return new UniqueIDGenerator(requestIdHeaderName, correlationIdHeaderName);
    }

    @Bean
    public SpringLoggingFilter loggingFilter() {
        return new SpringLoggingFilter(generator(), ignorePatterns, logHeaders);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnBean
    public RestTemplate existingRestTemplate(final RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<ClientHttpRequestInterceptor>();
        interceptorList.add(new RestTemplateSetHeaderInterceptor());
        restTemplate.setInterceptors(interceptorList);
        return restTemplate;
    }

    @Bean
    @ConditionalOnProperty("logging.loki.enabled")
    public Loki4jAppender lokiAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Loki4jAppender loki4jAppender = new Loki4jAppender();
        loki4jAppender.setContext(loggerContext);
        loki4jAppender.setName(LOKI_APPENDER_NAME);
        JavaHttpSender httpSender = new JavaHttpSender();
        httpSender.setUrl(url);
        loki4jAppender.setHttp(httpSender);
        JsonEncoder encoder = new JsonEncoder();
        encoder.setContext(loggerContext);
        AbstractLoki4jEncoder.LabelCfg label = new AbstractLoki4jEncoder.LabelCfg();
        label.setReadMarkers(true);
        label.setPattern("app=" + name + ",host=${HOSTNAME},level=%level");
        encoder.setLabel(label);
        encoder.start();
        loki4jAppender.setFormat(encoder);
        loki4jAppender.start();
        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(loki4jAppender);
        return loki4jAppender;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public void setTrustStoreLocation(String trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getIgnorePatterns() {
        return ignorePatterns;
    }

    public void setIgnorePatterns(String ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

    public boolean isLogHeaders() {
        return logHeaders;
    }

    public void setLogHeaders(boolean logHeaders) {
        this.logHeaders = logHeaders;
    }

    public String getRequestIdHeaderName() {
        return requestIdHeaderName;
    }

    public void setRequestIdHeaderName(String requestIdHeaderName) {
        this.requestIdHeaderName = requestIdHeaderName;
    }

    public String getCorrelationIdHeaderName() {
        return correlationIdHeaderName;
    }

    public void setCorrelationIdHeaderName(String correlationIdHeaderName) {
        this.correlationIdHeaderName = correlationIdHeaderName;
    }
}
