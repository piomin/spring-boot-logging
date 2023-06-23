package pl.piomin.logging.reactive.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import pl.piomin.logging.reactive.filter.ReactiveSpringLoggingFilter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.piomin.logging.reactive.util.UniqueIDGenerator;

@Configuration
@ConfigurationProperties(prefix = "logging.logstash")
public class ReactiveSpringLoggingAutoConfiguration {

    private static final String LOGSTASH_APPENDER_NAME = "LOGSTASH";

    private String url = "localhost:8500";
    private String ignorePatterns;
    private boolean logHeaders;
    private boolean useContentLength = true;
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
    public ReactiveSpringLoggingFilter reactiveSpringLoggingFilter() {
        return new ReactiveSpringLoggingFilter(generator(), ignorePatterns, logHeaders, useContentLength);
    }

    @Bean
    @ConditionalOnProperty("logging.logstash.enabled")
    public LogstashTcpSocketAppender logstashAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        LogstashTcpSocketAppender logstashTcpSocketAppender = new LogstashTcpSocketAppender();
        logstashTcpSocketAppender.setName(LOGSTASH_APPENDER_NAME);
        logstashTcpSocketAppender.setContext(loggerContext);
        logstashTcpSocketAppender.addDestination(url);
        if (trustStoreLocation != null) {
            SSLConfiguration sslConfiguration = new SSLConfiguration();
            KeyStoreFactoryBean factory = new KeyStoreFactoryBean();
            factory.setLocation(trustStoreLocation);
            if (trustStorePassword != null)
                factory.setPassword(trustStorePassword);
            sslConfiguration.setTrustStore(factory);
            logstashTcpSocketAppender.setSsl(sslConfiguration);
        }
        LogstashEncoder encoder = new LogstashEncoder();
        encoder.setContext(loggerContext);
        encoder.setIncludeContext(true);
        encoder.setCustomFields("{\"appname\":\"" + name + "\"}");
        encoder.start();
        logstashTcpSocketAppender.setEncoder(encoder);
        logstashTcpSocketAppender.start();
        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(logstashTcpSocketAppender);
        return logstashTcpSocketAppender;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public boolean isUseContentLength() {
        return useContentLength;
    }

    public void setUseContentLength(boolean useContentLength) {
        this.useContentLength = useContentLength;
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
