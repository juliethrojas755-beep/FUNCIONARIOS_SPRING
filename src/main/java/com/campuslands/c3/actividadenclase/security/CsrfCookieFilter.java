package com.campuslands.c3.actividadenclase.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security genera el token CSRF de forma perezosa: si nada lo "lee" en la petición,
 * la cookie XSRF-TOKEN no se envía al navegador. Este filtro simplemente lo lee en cada
 * petición para forzar que la cookie siempre se mande, y así app.js y login.js puedan leerla
 * y devolverla en el encabezado X-XSRF-TOKEN de cada POST/PUT/DELETE.
 *
 * Es el patrón recomendado por la documentación de Spring Security para aplicaciones de una
 * sola página (SPA) que usan la cookie de CSRF en vez de incrustar el token en el HTML.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
