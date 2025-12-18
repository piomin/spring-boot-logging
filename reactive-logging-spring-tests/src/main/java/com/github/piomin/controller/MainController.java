package com.github.piomin.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class MainController {

    @GetMapping("/{id}")
    public Mono<String> findById(@PathVariable("id") Integer id) {
        return Mono.just("Hello-" + id);
    }

    @PostMapping
    public Mono<String> postWithId(@RequestBody Integer id) {
        return Mono.just("Hello-" + id);
    }

    @GetMapping("/req-param")
    public Mono<String> findByIdRequest(@RequestParam("id") Integer id) {
        return Mono.just("Hello-" + id);
    }

    @PostMapping(value = "/req-param", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> postWithIdRequest(@RequestParam("id") Integer id) {
        return Mono.just("Hello-" + id);
    }

    @PostMapping(value = "/form-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> handleFormData(@RequestPart("name") String name,
                                       @RequestPart("file") FilePart file) {
        return Mono.just(String.format("Received form data - Name: %s, File: %s", name, file.filename()));
    }
}
