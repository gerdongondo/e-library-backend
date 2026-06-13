package com.luv2code.springbootlibrary.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/test")
    public String test() {
        return "Debug endpoint works! Time: " + new java.util.Date();
    }

    @PostMapping("/register-test")
    public String registerTest(@RequestBody String data) {
        return "Debug register works! Data: " + data;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}