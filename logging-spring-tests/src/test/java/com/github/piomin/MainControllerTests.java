package com.github.piomin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import pl.piomin.logging.config.SpringLoggingAutoConfiguration;
import pl.piomin.logging.filter.SpringLoggingFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainControllerTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    SpringLoggingAutoConfiguration c;

    @Test
    public void findById() {
        String res = restTemplate.getForObject("/test/{id}", String.class, 1);
        assertNotNull(res);
        assertEquals("Hello-1", res);
    }

    @Test
    public void findByIdReqParam() {
        String res = restTemplate.getForObject("/test/req-param?id={id}", String.class, 1);
        assertNotNull(res);
        assertEquals("Hello-1", res);
    }

    @Test
    public void postById() {
        String res = restTemplate.postForObject("/test", 1, String.class);
        assertNotNull(res);
        assertEquals("Hello-1", res);
    }

    @Test
    public void postByIdReqParam() {
        String res = restTemplate.postForObject("/test/req-param?id={id}", null, String.class, 1);
        assertNotNull(res);
        assertEquals("Hello-1", res);
    }
}
