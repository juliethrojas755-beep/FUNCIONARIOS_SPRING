package com.campuslands.c3.actividadenclase.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Corta los intentos de login antes de que lleguen a Spring Security cuando una IP ya
 * superó su límite (ver IpAttemptTracker). Así esa IP no puede seguir probando contraseñas
 * contra ninguna cuenta mientras dure el límite, sin tener que esperar a que la cuenta
 * objetivo se bloquee primero.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final String rutaLogin;
    private final IpAttemptTracker ipAttemptTracker;

    public LoginRateLimitFilter(String rutaLogin, IpAttemptTracker ipAttemptTracker) {
        this.rutaLogin = rutaLogin;
        this.ipAttemptTracker = ipAttemptTracker;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean esIntentoDeLogin = "POST".equalsIgnoreCase(request.getMethod())
                && rutaLogin.equals(request.getRequestURI());

        if (esIntentoDeLogin && ipAttemptTracker.estaLimitada(request.getRemoteAddr())) {
            JsonResponder.escribir(response, 429, "mensaje",
                    "Demasiados intentos desde este dispositivo. Intenta de nuevo más tarde.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
