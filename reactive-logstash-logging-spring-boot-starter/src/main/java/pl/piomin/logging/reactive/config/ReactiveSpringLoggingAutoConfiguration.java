package pl.piomin.logging.reactive.config;

import org.springframework.beans.factory.annotation.Value;
import pl.piomin.logging.reactive.filter.ReactiveSpringLoggingFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.piomin.logging.reactive.util.UniqueIDGenerator;

@Configuration
public class ReactiveSpringLoggingAutoConfiguration {

	private static final String LOGSTASH_APPENDER_NAME = "LOGSTASH";

	private String url = "localhost:8500";
	private String ignorePatterns;
	private boolean logHeaders;
	private String trustStoreLocation;
	private String trustStorePassword;
	@Value("${spring.application.name:-}")
	String name;

	@Bean
	public UniqueIDGenerator generator() {
		return new UniqueIDGenerator();
	}

	@Bean
	public ReactiveSpringLoggingFilter reactiveSpringLoggingFilter() {
		return new ReactiveSpringLoggingFilter(generator(), ignorePatterns, logHeaders);
	}

}
