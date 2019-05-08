package pl.piomin.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import pl.piomin.logging.filter.SpringLoggingFilter;
import pl.piomin.logging.util.UniqueIDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class SpringLoggingAutoConfiguration {

	private static final String LOGSTASH_APPENDER_NAME = "LOGSTASH";

	@Value("${spring.logstash.url:localhost:8500}")
	String url;
	@Value("${spring.application.name:-}")
	String name;
	@Value("${spring.logstash.ssl.trustStoreLocation}")
	Optional<String> trustStoreLocation;
	@Value("${spring.logstash.ssl.trustStorePassword}")
	Optional<String> trustStorePassword;

	@Bean
	public UniqueIDGenerator generator() {
		return new UniqueIDGenerator();
	}

	@Bean
	public SpringLoggingFilter loggingFilter() {
		return new SpringLoggingFilter(generator());
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<ClientHttpRequestInterceptor>();
		restTemplate.setInterceptors(interceptorList);
		return restTemplate;
	}

	@Bean
	@ConditionalOnProperty("spring.logstash.enabled")
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

}
