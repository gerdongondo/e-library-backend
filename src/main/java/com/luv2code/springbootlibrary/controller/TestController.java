package com.luv2code.springbootlibrary.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public endpoint - accessible sans token";
    }

    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "Protected endpoint - besoin d'un token";
    }
}