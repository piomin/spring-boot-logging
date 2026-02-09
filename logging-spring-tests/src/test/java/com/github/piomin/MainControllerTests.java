package com.github.piomin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import pl.piomin.logging.MemoryAppender;
import pl.piomin.logging.filter.SpringLoggingFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@AutoConfigureTestRestTemplate
public class MainControllerTests {

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    RestTestClient restTestClient;
    MemoryAppender memoryAppender;

    @BeforeEach
    void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(SpringLoggingFilter.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    public void findById() {
        restTestClient.get().uri("/test/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello-1");
        assertEquals(2, memoryAppender.getSize());
    }

    @Test
    public void findByIdReqParam() {
        restTestClient.get().uri("/test/req-param?id={id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello-1");
        assertEquals(2, memoryAppender.getSize());
        assertFalse(memoryAppender.search("payload=id=1").isEmpty());
    }

    @Test
    public void postById() {
        restTestClient.post().uri("/test").body(1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello-1");
        assertEquals(2, memoryAppender.getSize());
        assertFalse(memoryAppender.search("payload=1").isEmpty());
    }

    @Test
    public void postByIdReqParam() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Integer> map = new LinkedMultiValueMap<>();
        map.add("id", 1);
        HttpEntity<MultiValueMap<String, Integer>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> res = testRestTemplate.exchange("/test/req-param", HttpMethod.POST, entity, String.class);
        assertNotNull(res);
        assertNotNull(res.getBody());
        assertEquals("Hello-1", res.getBody());
        assertEquals(2, memoryAppender.getSize());
    }

    @Test
    public void postMultipart() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", getTestFile());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> res = testRestTemplate
                .postForEntity("/test/req-file", requestEntity, String.class);
        assertNotNull(res);
        assertNotNull(res.getBody());
        assertTrue(res.getStatusCode().is2xxSuccessful());
        assertEquals(2, memoryAppender.getSize());
    }

    public Resource getTestFile() throws IOException {
        Path testFile = Files.createTempFile("test-file", ".txt");
        Files.write(testFile, "Hello World !!, This is a test file.".getBytes());
        return new FileSystemResource(testFile.toFile());
    }

}
