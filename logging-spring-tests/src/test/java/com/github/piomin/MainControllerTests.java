package com.github.piomin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainControllerTests {

    @Autowired
    TestRestTemplate restTemplate;

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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Integer> map = new LinkedMultiValueMap<>();
        map.add("id", 1);
        HttpEntity<MultiValueMap<String, Integer>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> res = restTemplate.exchange("/test/req-param", HttpMethod.POST, entity, String.class);
        assertNotNull(res);
        assertNotNull(res.getBody());
        assertEquals("Hello-1", res.getBody());
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
    }

    public Resource getTestFile() throws IOException {
        Path testFile = Files.createTempFile("test-file", ".txt");
        Files.write(testFile, "Hello World !!, This is a test file.".getBytes());
        return new FileSystemResource(testFile.toFile());
    }
}
