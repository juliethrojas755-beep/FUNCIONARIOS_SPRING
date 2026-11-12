package com.campuslands.c3.actividadenclase.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Cuando una petición a /api/** llega sin sesión (o con una sesión vencida), responde JSON 401
 * en vez de redirigir al login.html: app.js lo detecta y manda a la persona a iniciar sesión,
 * pero una llamada fetch no puede "seguir" una redirección a una página HTML de forma útil.
 */
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException excepcion)
            throws IOException {
        JsonResponder.escribir(response, HttpServletResponse.SC_UNAUTHORIZED,
                "mensaje", "Tu sesión no es válida o venció. Inicia sesión de nuevo.");
    }
}
