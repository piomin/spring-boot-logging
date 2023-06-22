package com.github.piomin.controller;

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
}
