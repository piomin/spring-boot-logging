package com.github.piomin.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public String findByIdRequest(@RequestParam("id") Integer id) {
        return "Hello-" + id;
    }

    @PostMapping(value = "/req-param", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String postWithIdRequest(@RequestParam("id") Integer id) {
        return "Hello-" + id;
    }

    @PostMapping(value = "/req-file")
    public String processFile(@RequestParam("file") MultipartFile file) throws IOException {
        return file.getOriginalFilename() + file.getBytes().length;
    }

}
