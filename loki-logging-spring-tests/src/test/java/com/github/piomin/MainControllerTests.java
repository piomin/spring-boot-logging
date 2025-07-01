package com.github.piomin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import pl.piomin.logging.filter.SpringLoggingFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainControllerTests {

    @Autowired
    TestRestTemplate restTemplate;
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
        String res = restTemplate.getForObject("/test/{id}", String.class, 1);
        assertNotNull(res);
        assertEquals("Hello-1", res);
        assertEquals(2, memoryAppender.getSize());
    }

    @Test
    public void findByIdReqParam() {
        String res = restTemplate.getForObject("/test/req-param?id={id}", String.class, 1);
        assertNotNull(res);
        assertEquals("Hello-1", res);
        assertEquals(2, memoryAppender.getSize());
        assertFalse(memoryAppender.search("payload=id=1").isEmpty());
    }

    @Test
    public void postById() {
        String res = restTemplate.postForObject("/test", 1, String.class);
        assertNotNull(res);
        assertEquals("Hello-1", res);
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

        ResponseEntity<String> res = restTemplate.exchange("/test/req-param", HttpMethod.POST, entity, String.class);
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

        ResponseEntity<String> res = restTemplate
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
