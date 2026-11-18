package com.example.huellitasVet.restController;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestControllerAdvice
public class ErroresApi {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), "Revisa el valor ingresado."));
        return Map.of("mensaje", "Revisa los campos indicados.", "campos", campos);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> formato() {
        return Map.of("mensaje", "El formato de los datos es incorrecto.");
    }

    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> concurrente() {
        return Map.of(
                "mensaje", "Otra operación cambió este registro. Vuelve a consultar el detalle.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> integridad() {
        return Map.of("mensaje", "Hay un registro duplicado o relacionado con esta operación.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> negocio(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("mensaje", Objects.toString(ex.getReason(), "Operación rechazada.")));
    }

    public static ResponseStatusException ausente(String nombre) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, nombre + " no encontrado.");
    }

    public static ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
