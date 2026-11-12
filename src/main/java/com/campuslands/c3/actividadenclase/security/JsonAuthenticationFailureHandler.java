package com.campuslands.c3.actividadenclase.security;

import com.campuslands.c3.actividadenclase.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Traduce cada intento de login fallido a una respuesta JSON clara, sin revelar si la cuenta
 * existe: tanto un usuario que no existe como una contraseña incorrecta devuelven el mismo
 * mensaje genérico. La única excepción es cuando la cuenta YA estaba bloqueada: ahí sí se
 * informa cuánto falta para poder reintentar, porque es información que el dueño de esa
 * cuenta necesita (y que, en la práctica, ya bloquearla confirma que existía).
 */
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String MENSAJE_GENERICO = "Usuario o contraseña incorrectos.";

    private final UsuarioService usuarioService;
    private final IpAttemptTracker ipAttemptTracker;

    public JsonAuthenticationFailureHandler(UsuarioService usuarioService, IpAttemptTracker ipAttemptTracker) {
        this.usuarioService = usuarioService;
        this.ipAttemptTracker = ipAttemptTracker;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException excepcion)
            throws IOException {

        ipAttemptTracker.registrarFallo(request.getRemoteAddr());
        String username = request.getParameter("username");

        if (excepcion instanceof LockedException) {
            // La cuenta ya estaba bloqueada antes de este intento (Spring Security ni siquiera
            // llegó a comparar la contraseña): no sumamos otro intento, solo avisamos cuánto falta.
            LocalDateTime bloqueadoHasta = username == null || username.isBlank()
                    ? null
                    : usuarioService.obtenerBloqueadoHasta(username);
            JsonResponder.escribir(response, 423, "mensaje", mensajeDeBloqueo(bloqueadoHasta));
            return;
        }

        LocalDateTime bloqueadoHasta = username == null || username.isBlank()
                ? null
                : usuarioService.registrarIntentoFallido(username);

        if (bloqueadoHasta != null) {
            JsonResponder.escribir(response, 423, "mensaje", mensajeDeBloqueo(bloqueadoHasta));
        } else {
            JsonResponder.escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "mensaje", MENSAJE_GENERICO);
        }
    }

    private String mensajeDeBloqueo(LocalDateTime bloqueadoHasta) {
        if (bloqueadoHasta == null) {
            return "Esta cuenta está bloqueada temporalmente por demasiados intentos fallidos. Intenta de nuevo más tarde.";
        }
        long minutos = Math.max(1, Duration.between(LocalDateTime.now(), bloqueadoHasta).toMinutes());
        return "Demasiados intentos fallidos. Intenta de nuevo en " + minutos
                + (minutos == 1 ? " minuto." : " minutos.");
    }
}
