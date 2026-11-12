package com.campuslands.c3.actividadenclase.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/** Responde JSON tras cerrar sesión, en vez de redirigir (la sesión se invalida en SecurityConfig). */
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        JsonResponder.escribir(response, HttpServletResponse.SC_OK, "mensaje", "Sesión cerrada.");
    }
}
