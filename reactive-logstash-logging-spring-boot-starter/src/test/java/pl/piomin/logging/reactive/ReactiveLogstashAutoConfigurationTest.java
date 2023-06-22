package pl.piomin.logging.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pl.piomin.logging.reactive.config.ReactiveSpringLoggingAutoConfiguration;

@SpringBootTest(classes = ReactiveSpringLoggingAutoConfiguration.class)
public class ReactiveLogstashAutoConfigurationTest {

    @Test
    public void startup() {

    }
}
