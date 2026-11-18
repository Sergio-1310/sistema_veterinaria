package com.example.huellitasVet;

import static org.junit.jupiter.api.Assertions.*;

import com.example.huellitasVet.model.Usuario;
import com.example.huellitasVet.repository.UsuarioRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:clinica;MODE=MySQL;DB_CLOSE_DELAY=-1",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class HuellitasVetApplicationTests {
    @LocalServerPort int puerto;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    final JsonMapper json = JsonMapper.builder().build();
    final HttpClient http = HttpClient.newHttpClient();
    final String clave = UUID.randomUUID().toString();

    @DynamicPropertySource
    static void configurar(DynamicPropertyRegistry props) {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        props.add("app.jwt.secret", () -> Base64.getEncoder().encodeToString(bytes));
    }

    HttpResponse<String> enviar(String metodo, String ruta, String token, Object datos)
            throws Exception {
        var builder =
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                        .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var cuerpo =
                datos == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(datos));
        return http.send(
                builder.method(metodo, cuerpo).build(), HttpResponse.BodyHandlers.ofString());
    }

    String llamar(int esperado, String metodo, String ruta, String token, Object datos)
            throws Exception {
        var respuesta = enviar(metodo, ruta, token, datos);
        assertEquals(
                esperado,
                respuesta.statusCode(),
                metodo
                        + " "
                        + ruta
                        + " "
                        + (esperado == respuesta.statusCode() ? "" : respuesta.body()));
        return respuesta.body();
    }

    long id(String respuesta) {
        return json.readTree(respuesta).get("id").asLong();
    }

    String sesion(String correo) throws Exception {
        return json.readTree(
                        llamar(
                                200,
                                "POST",
                                "/api/auth/login",
                                null,
                                Map.of("email", correo, "password", clave)))
                .get("token")
                .asString();
    }

    @Test
    void flujoDeRecepcionClinicaYAdministracion() throws Exception {
        var admin = new Usuario();
        admin.setNombre("Administración");
        admin.setEmail("admin@test.invalid");
        admin.setRol("ADMIN");
        admin.setPassword(encoder.encode(clave));
        usuarios.save(admin);
        String a = sesion(admin.getEmail());
        String recepcion = "recepcion@test.invalid", medico = "medico@test.invalid";
        llamar(401, "GET", "/api/mascotas", null, null);
        llamar(401, "POST", "/api/auth/login", null, Map.of("email", recepcion, "password", clave));
        long uid =
                id(
                        llamar(
                                201,
                                "POST",
                                "/api/usuarios",
                                a,
                                Map.of(
                                        "nombre",
                                        "Recepción",
                                        "email",
                                        recepcion,
                                        "rol",
                                        "RECEPCIONISTA",
                                        "password",
                                        clave)));
        llamar(
                201,
                "POST",
                "/api/usuarios",
                a,
                Map.of(
                        "nombre",
                        "Medicina",
                        "email",
                        medico,
                        "rol",
                        "VETERINARIO",
                        "password",
                        clave));
        String r = sesion(recepcion), v = sesion(medico);
        assertFalse(llamar(200, "GET", "/api/usuarios", a, null).contains("password"));
        llamar(403, "GET", "/api/usuarios", r, null);
        llamar(403, "POST", "/veterinarios/guardar", r, Map.of());
        llamar(400, "POST", "/api/propietarios", r, Map.of());
        var persona =
                Map.of(
                        "nombres",
                        "Sol",
                        "apellidos",
                        "Ríos",
                        "telefono",
                        "999111222",
                        "correo",
                        "sol@test.invalid");
        long propietario = id(llamar(201, "POST", "/api/propietarios", r, persona));
        llamar(409, "POST", "/api/propietarios", r, persona);
        var pet = Map.of("nombre", "Luna", "especie", "Gato", "propietarioId", propietario);
        long mascota = id(llamar(201, "POST", "/api/mascotas", r, pet));
        llamar(200, "PUT", "/api/mascotas/" + mascota, r, pet);
        llamar(409, "DELETE", "/api/propietarios/" + propietario, r, null);
        long vet =
                id(
                        llamar(
                                201,
                                "POST",
                                "/api/veterinarios",
                                a,
                                Map.of("nombres", "Ana", "apellidos", "Vega", "correo", medico)));
        var turno =
                new HashMap<String, Object>(
                        Map.of(
                                "mascotaId",
                                mascota,
                                "veterinarioId",
                                vet,
                                "fecha",
                                LocalDate.now().plusDays(1).toString(),
                                "hora",
                                "10:00",
                                "motivo",
                                "Control"));
        long cita = id(llamar(201, "POST", "/api/citas", r, turno));
        llamar(409, "POST", "/api/citas", r, turno);
        turno.put("hora", "11:00");
        llamar(200, "PUT", "/api/citas/" + cita, r, turno);
        String atencion = "/api/citas/" + cita + "/atencion";
        var consulta = Map.of("diagnostico", "Evaluación normal", "tratamiento", "Control anual");
        llamar(403, "POST", atencion, r, consulta);
        long ficha = id(llamar(201, "POST", atencion, v, consulta));
        llamar(409, "POST", atencion, v, consulta);
        llamar(409, "DELETE", "/api/citas/" + cita, r, null);
        assertTrue(llamar(200, "GET", "/api/historial/" + ficha, v, null).contains("ATENDIDA"));
        long segunda = id(llamar(201, "POST", "/api/citas", r, turno));
        llamar(204, "DELETE", "/api/citas/" + segunda, r, null);
        llamar(409, "POST", "/api/citas/" + segunda + "/atencion", v, consulta);
        llamar(200, "PATCH", "/api/veterinarios/" + vet + "/activo", a, Map.of("activo", false));
        llamar(409, "POST", "/api/citas", r, turno);
        llamar(
                204,
                "PUT",
                "/api/usuarios/" + uid + "/password",
                a,
                Map.of("password", UUID.randomUUID().toString()));
        llamar(401, "GET", "/api/auth/me", r, null);
        llamar(204, "PUT", "/api/usuarios/" + uid + "/password", a, Map.of("password", clave));
        r = sesion(recepcion);
        llamar(200, "PATCH", "/api/usuarios/" + uid + "/activo", a, Map.of("activo", false));
        llamar(401, "GET", "/api/dashboard", r, null);
        llamar(401, "POST", "/api/auth/login", null, Map.of("email", recepcion, "password", clave));
    }
}
