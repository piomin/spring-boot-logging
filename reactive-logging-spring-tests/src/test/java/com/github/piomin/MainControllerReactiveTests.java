package com.github.piomin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import pl.piomin.logging.MemoryAppender;
import pl.piomin.logging.reactive.filter.ReactiveSpringLoggingFilter;
import pl.piomin.logging.reactive.interceptor.RequestLoggingInterceptor;
import pl.piomin.logging.reactive.interceptor.ResponseLoggingInterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class MainControllerReactiveTests {

    @Autowired
    WebTestClient webTestClient;
    MemoryAppender memoryAppender;

    @BeforeEach
    void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(ReactiveSpringLoggingFilter.class);
        Logger logger2 = (Logger) LoggerFactory.getLogger(RequestLoggingInterceptor.class);
        Logger logger3 = (Logger) LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        logger2.setLevel(Level.DEBUG);
        logger2.addAppender(memoryAppender);
        logger3.setLevel(Level.DEBUG);
        logger3.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    public void findById() {
        String res = webTestClient.get()
                .uri("/test/{id}", 1)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        assertEquals("Hello-1", res);
        assertEquals(2, memoryAppender.getSize());
    }

    @Test
    public void findByIdReqParam() {
        String res = webTestClient.get()
                .uri("/test/req-param?id={id}", 1)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        assertEquals("Hello-1", res);
        assertEquals(2, memoryAppender.getSize());
        assertFalse(memoryAppender.search("payload={id=1}").isEmpty());
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
        assertEquals(2, memoryAppender.getSize());
        assertFalse(memoryAppender.search("payload=1").isEmpty());
    }

//    @Test
    public void postByIdReqParam() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("id", "1");

        String res = webTestClient.post()
                .uri("/test/req-param")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertEquals("Hello-1", res);
        assertEquals(2, memoryAppender.getSize());
        assertFalse(memoryAppender.search("payload=1").isEmpty());
    }

    @Test
    public void handleFormData() throws Exception {
        // Create a test file
        String testContent = "test file content";
        String fileName = "test.txt";
        String name = "test-user";

        // Create a mock file part
        byte[] fileContent = testContent.getBytes();
        Resource resource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        // Create form data
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("name", name);
        formData.add("file", resource);

        // Send request and verify response
        String res = webTestClient.post()
                .uri("/test/form-data")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        // Verify response
        String expectedResponse = String.format("Received form data - Name: %s, File: %s", name, fileName);
        assertEquals(expectedResponse, res);

        // Verify logging
        assertEquals(3, memoryAppender.getSize());
        assertFalse(memoryAppender.search("payload=").isEmpty());
    }
}
