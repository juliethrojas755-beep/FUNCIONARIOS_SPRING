package com.campuslands.c3.actividadenclase.controller;

import com.campuslands.c3.actividadenclase.dto.RegistroRequest;
import com.campuslands.c3.actividadenclase.model.Usuario;
import com.campuslands.c3.actividadenclase.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro de cuentas y consulta de la sesión actual. El login y el logout los maneja
 * Spring Security directamente (ver SecurityConfig): no hacen falta métodos aquí para esos.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        Usuario creado = usuarioService.registrar(solicitud);
        return ResponseEntity.status(201).body(Map.of("username", creado.getUsername()));
    }

    /** Le dice al frontend si hay una sesión activa y con qué usuario, sin exigir que la haya. */
    @GetMapping("/me")
    public Map<String, Object> usuarioActual(Authentication authentication) {
        boolean autenticado = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
        if (!autenticado) {
            return Map.of("autenticado", false);
        }
        return Map.of("autenticado", true, "username", authentication.getName());
    }
}
