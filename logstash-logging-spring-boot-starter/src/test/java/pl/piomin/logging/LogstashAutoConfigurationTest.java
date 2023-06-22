package pl.piomin.logging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import pl.piomin.logging.config.SpringLoggingAutoConfiguration;
import pl.piomin.logging.filter.SpringLoggingFilter;
import pl.piomin.logging.util.UniqueIDGenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SpringLoggingAutoConfiguration.class)
public class LogstashAutoConfigurationTest {

    @Autowired
    UniqueIDGenerator generator;
    @Autowired
    SpringLoggingFilter loggingFilter;
    @Autowired
    RestTemplate restTemplate;

    // TODO - enable after springboot update
//    public LogstashAutoConfigurationTest(UniqueIDGenerator generator, SpringLoggingFilter loggingFilter, RestTemplate restTemplate) {
//        this.generator = generator;
//        this.loggingFilter = loggingFilter;
//        this.restTemplate = restTemplate;
//    }

    @Test
    public void startup() {
        assertNotNull(generator);
        assertNotNull(loggingFilter);
        assertNotNull(restTemplate);
    }

}
