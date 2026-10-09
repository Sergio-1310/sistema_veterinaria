package com.example.huellitasVet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/propietarios")
public class PropietarioController {

    @GetMapping
    public String vistaPropietarios() {
        return "propietarios/index"; // Carga la plantilla HTML templates/propietarios.html
    }
}