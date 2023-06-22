package com.github.piomin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainControllerTests {

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void findById() {
        String res = webTestClient.get()
                .uri("/test/{id}", 1)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        assertEquals("Hello-1", res);
    }

    @Test
    public void postById() {
        String res = webTestClient.post()
                .uri("/test")
                .bodyValue(1)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        assertEquals("Hello-1", res);
    }
}
