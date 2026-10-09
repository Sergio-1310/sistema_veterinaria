package com.example.huellitasVet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/propietarios")
public class PropietarioController {

    @GetMapping
    public String vistaPropietarios(@RequestHeader(value = "HX-Request", required = false) boolean isHtmx) {
        if (isHtmx) {
            // Petición AJAX desde HTMX: devuelve SOLO el bloque <main> (sin recargar layout ni sidebar)
            return "propietarios :: content";
        }
        // Navegación normal/inicial: devuelve toda la página decorada con el layout base
        return "propietarios/index";
    }
}