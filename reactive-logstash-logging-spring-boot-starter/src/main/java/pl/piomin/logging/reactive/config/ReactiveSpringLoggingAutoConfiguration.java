package pl.piomin.logging.reactive.config;

import pl.piomin.logging.reactive.filter.ReactiveSpringLoggingFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveSpringLoggingAutoConfiguration {

	@Bean
	public ReactiveSpringLoggingFilter reactiveSpringLoggingFilter() {
		return new ReactiveSpringLoggingFilter();
	}

}
