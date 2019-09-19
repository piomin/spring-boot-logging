package pl.piomin.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "logging.logstash")
public class SpringLoggingAutoConfiguration {

	private static final String LOGSTASH_APPENDER_NAME = "LOGSTASH";

	private String url = "localhost:8500";
	private Optional<String> ignorePatterns;
	private boolean logHeaders;
	private Optional<String> trustStoreLocation;
	private Optional<String> trustStorePassword;
	@Value("${spring.application.name:-}")
	String name;
	@Autowired(required = false)
	Optional<RestTemplate> template;

	@Bean
	public UniqueIDGenerator generator() {
		return new UniqueIDGenerator();
	}

	@Bean
	public SpringLoggingFilter loggingFilter() {
		return new SpringLoggingFilter(generator(), ignorePatterns, logHeaders);
	}

	@Bean
	@ConditionalOnMissingBean(RestTemplate.class)
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<ClientHttpRequestInterceptor>();
		interceptorList.add(new RestTemplateSetHeaderInterceptor());
		restTemplate.setInterceptors(interceptorList);
		return restTemplate;
	}

	@Bean
	@ConditionalOnProperty("logging.logstash.enabled")
	public LogstashTcpSocketAppender logstashAppender() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		LogstashTcpSocketAppender logstashTcpSocketAppender = new LogstashTcpSocketAppender();
		logstashTcpSocketAppender.setName(LOGSTASH_APPENDER_NAME);
		logstashTcpSocketAppender.setContext(loggerContext);
		logstashTcpSocketAppender.addDestination(url);
		trustStoreLocation.ifPresent(it -> {
			SSLConfiguration sslConfiguration = new SSLConfiguration();
			KeyStoreFactoryBean factory = new KeyStoreFactoryBean();
			factory.setLocation(it);
			trustStorePassword.ifPresent(factory::setPassword);
			sslConfiguration.setTrustStore(factory);
			logstashTcpSocketAppender.setSsl(sslConfiguration);
		});
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

	@PostConstruct
	public void init() {
		template.ifPresent(restTemplate -> {
			List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<ClientHttpRequestInterceptor>();
			interceptorList.add(new RestTemplateSetHeaderInterceptor());
			restTemplate.setInterceptors(interceptorList);
		});
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Optional<String> getTrustStoreLocation() {
		return trustStoreLocation;
	}

	public void setTrustStoreLocation(Optional<String> trustStoreLocation) {
		this.trustStoreLocation = trustStoreLocation;
	}

	public Optional<String> getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(Optional<String> trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public Optional<String> getIgnorePatterns() {
		return ignorePatterns;
	}

	public void setIgnorePatterns(Optional<String> ignorePatterns) {
		this.ignorePatterns = ignorePatterns;
	}

	public boolean isLogHeaders() {
		return logHeaders;
	}

	public void setLogHeaders(boolean logHeaders) {
		this.logHeaders = logHeaders;
	}
}
