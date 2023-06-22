package pl.piomin.logging.reactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pl.piomin.logging.reactive.config.ReactiveSpringLoggingAutoConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveSpringLoggingAutoConfiguration.class)
public class ReactiveLogstashAutoConfigurationTest {

    @Test
    public void startup() {

    }
}
