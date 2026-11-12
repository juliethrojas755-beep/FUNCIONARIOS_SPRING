package com.campuslands.c3.actividadenclase.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Límite de intentos de login por dirección IP, guardado en memoria (no en MySQL: es una
 * protección rápida contra abuso, no un dato de negocio que deba sobrevivir un reinicio).
 *
 * Esto es aparte del bloqueo por cuenta en UsuarioService. Sin esto, alguien podría escribir
 * mal la contraseña de CUALQUIER cuenta que conozca (o intente adivinar) muchas veces seguidas
 * y bloquearla a propósito, sin que nada se lo impida. Limitar también por IP hace que ese
 * abuso masivo se frene antes de llegar a bloquear cuentas de otras personas.
 *
 * Nota: identifica la IP con request.getRemoteAddr(), es decir, la conexión directa al
 * servidor. Si la aplicación se sirve detrás de un proxy o balanceador, haría falta confiar
 * en un encabezado como X-Forwarded-For (configurado con cuidado, para que no se pueda
 * falsificar) en vez de esto.
 */
@Component
public class IpAttemptTracker {

    private record Intentos(int conteo, Instant inicioVentana) {
    }

    private final Map<String, Intentos> porIp = new ConcurrentHashMap<>();
    private final LockoutProperties configuracion;

    public IpAttemptTracker(LockoutProperties configuracion) {
        this.configuracion = configuracion;
    }

    /** true si esta IP ya superó el máximo de intentos fallidos permitidos en la ventana actual. */
    public boolean estaLimitada(String ip) {
        Intentos actuales = porIp.get(ip);
        if (actuales == null) {
            return false;
        }
        if (ventanaExpiro(actuales)) {
            porIp.remove(ip, actuales);
            return false;
        }
        return actuales.conteo() >= configuracion.getMaxIntentosPorIp();
    }

    /** Registra un intento fallido desde esa IP. */
    public void registrarFallo(String ip) {
        porIp.compute(ip, (clave, actuales) -> {
            if (actuales == null || ventanaExpiro(actuales)) {
                return new Intentos(1, Instant.now());
            }
            return new Intentos(actuales.conteo() + 1, actuales.inicioVentana());
        });
    }

    private boolean ventanaExpiro(Intentos intentos) {
        Duration ventana = Duration.ofMinutes(configuracion.getMinutosVentanaIp());
        return Instant.now().isAfter(intentos.inicioVentana().plus(ventana));
    }
}
