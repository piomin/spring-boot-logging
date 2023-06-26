package com.github.piomin.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class MainController {

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Integer id) {
        return "Hello-" + id;
    }

    @PostMapping
    public String postWithId(@RequestBody Integer id) {
        return "Hello-" + id;
    }

    @GetMapping("/req-param")
    public String findByIdRequest(@RequestParam Integer id) {
        return "Hello-" + id;
    }

    @PostMapping(value = "/req-param", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String postWithIdRequest(@RequestParam Integer id) {
        return "Hello-" + id;
    }
}
