package pl.piomin.logging.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.piomin.logging.reactive.config.ReactiveSpringLoggingAutoConfiguration;
import pl.piomin.logging.reactive.filter.ReactiveSpringLoggingFilter;
import pl.piomin.logging.reactive.util.UniqueIDGenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ReactiveSpringLoggingAutoConfiguration.class)
public class ReactiveLogstashAutoConfigurationTest {

    @Autowired
    UniqueIDGenerator generator;
    @Autowired
    ReactiveSpringLoggingFilter loggingFilter;

    @Test
    public void startup() {
        assertNotNull(generator);
        assertNotNull(loggingFilter);
    }
}
