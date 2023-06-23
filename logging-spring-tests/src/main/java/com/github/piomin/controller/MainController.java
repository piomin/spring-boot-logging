package com.github.piomin.controller;

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

    @PostMapping("/req-param")
    public String postWithIdRequest(@RequestParam Integer id) {
        return "Hello-" + id;
    }
}
