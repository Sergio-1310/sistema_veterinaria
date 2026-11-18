package com.example.huellitasVet.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @Value("${app.angular-origin}")
    private String origen;

    @GetMapping({"/", "/login"})
    public String inicio() {
        return "redirect:" + origen + "/inicio";
    }
}
