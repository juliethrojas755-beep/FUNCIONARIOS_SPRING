package com.campuslands.c3.actividadenclase.security;

import com.campuslands.c3.actividadenclase.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * login.js hace la petición de inicio de sesión con fetch, no es un formulario HTML normal,
 * así que en vez de la redirección por defecto de Spring Security respondemos JSON 200.
 */
public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    public JsonAuthenticationSuccessHandler(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        usuarioService.registrarIntentoExitoso(authentication.getName());
        JsonResponder.escribir(response, HttpServletResponse.SC_OK, "username", authentication.getName());
    }
}
