package com.campuslands.c3.actividadenclase.security;

import com.campuslands.c3.actividadenclase.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
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
            responderBloqueo(response, bloqueadoHasta);
            return;
        }

        LocalDateTime bloqueadoHasta = username == null || username.isBlank()
                ? null
                : usuarioService.registrarIntentoFallido(username);

        if (bloqueadoHasta != null) {
            responderBloqueo(response, bloqueadoHasta);
        } else {
            JsonResponder.escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "mensaje", MENSAJE_GENERICO);
        }
    }

    /**
     * Responde 423 (Locked) con el mensaje y los segundos que faltan, para que login.js
     * muestre la cuenta regresiva y desactive el botón mientras dura el bloqueo.
     */
    private void responderBloqueo(HttpServletResponse response, LocalDateTime bloqueadoHasta) throws IOException {
        long segundos = segundosRestantes(bloqueadoHasta);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", "Demasiados intentos fallidos. Cuenta bloqueada, intenta de nuevo en "
                + segundos + (segundos == 1 ? " segundo." : " segundos."));
        cuerpo.put("segundosRestantes", segundos);
        JsonResponder.escribir(response, 423, cuerpo);
    }

    private long segundosRestantes(LocalDateTime bloqueadoHasta) {
        if (bloqueadoHasta == null) {
            return 1;
        }
        long milisegundos = Duration.between(LocalDateTime.now(), bloqueadoHasta).toMillis();
        return Math.max(1, (milisegundos + 999) / 1000); // redondea hacia arriba
    }
}
