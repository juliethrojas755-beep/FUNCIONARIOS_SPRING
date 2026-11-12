package com.campuslands.c3.actividadenclase.service;

import com.campuslands.c3.actividadenclase.dto.RegistroRequest;
import com.campuslands.c3.actividadenclase.exception.SolicitudInvalidaException;
import com.campuslands.c3.actividadenclase.exception.UsuarioYaExisteException;
import com.campuslands.c3.actividadenclase.model.Usuario;
import com.campuslands.c3.actividadenclase.repository.UsuarioRepository;
import com.campuslands.c3.actividadenclase.security.LockoutProperties;
import com.campuslands.c3.actividadenclase.security.PasswordPolicyProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de cuentas de usuario: registro, política de contraseñas y control de
 * intentos fallidos. El conteo de intentos y el bloqueo se guardan en MySQL
 * (tabla usuarios), así que sobreviven a un reinicio de la aplicación.
 */
@Service
public class UsuarioService {

    private static final Pattern TIENE_MAYUSCULA = Pattern.compile("[A-ZÁÉÍÓÚÑ]");
    private static final Pattern TIENE_MINUSCULA = Pattern.compile("[a-záéíóúñ]");
    private static final Pattern TIENE_NUMERO = Pattern.compile("[0-9]");
    private static final Pattern TIENE_ESPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyProperties politicaContrasena;
    private final LockoutProperties bloqueo;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicyProperties politicaContrasena,
            LockoutProperties bloqueo) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.politicaContrasena = politicaContrasena;
        this.bloqueo = bloqueo;
    }

    /**
     * Crea una cuenta nueva. Valida la contraseña contra la política configurada y
     * rechaza nombres de usuario repetidos (sin distinguir mayúsculas/minúsculas).
     */
    @Transactional
    public Usuario registrar(RegistroRequest solicitud) {
        String username = solicitud.getUsername().trim();
        validarContrasena(solicitud.getPassword());

        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso.");
        }

        Usuario usuario = new Usuario(username, passwordEncoder.encode(solicitud.getPassword()));
        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException excepcion) {
            // Dos registros con el mismo nombre casi al mismo tiempo: la restricción UNIQUE de MySQL
            // atrapa el caso que la comprobación de arriba pudo no alcanzar a ver.
            throw new UsuarioYaExisteException("El nombre de usuario ya está en uso.");
        }
    }

    /** Valida la contraseña contra la política configurable. Lanza SolicitudInvalidaException si falla. */
    public void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new SolicitudInvalidaException("La contraseña es obligatoria.");
        }
        if (contrasena.length() < politicaContrasena.getLongitudMinima()
                || contrasena.length() > politicaContrasena.getLongitudMaxima()) {
            throw new SolicitudInvalidaException(
                    "La contraseña no cumple los requisitos: " + politicaContrasena.describirRequisitos());
        }
        boolean cumple =
                (!politicaContrasena.isRequiereMayuscula() || TIENE_MAYUSCULA.matcher(contrasena).find())
                && (!politicaContrasena.isRequiereMinuscula() || TIENE_MINUSCULA.matcher(contrasena).find())
                && (!politicaContrasena.isRequiereNumero() || TIENE_NUMERO.matcher(contrasena).find())
                && (!politicaContrasena.isRequiereCaracterEspecial() || TIENE_ESPECIAL.matcher(contrasena).find());
        if (!cumple) {
            throw new SolicitudInvalidaException(
                    "La contraseña no cumple los requisitos: " + politicaContrasena.describirRequisitos());
        }
    }

    /**
     * Se llama tras un inicio de sesión exitoso: reinicia el contador y quita cualquier bloqueo.
     * Usa bloqueo pesimista para que una petición concurrente con un intento fallido de la misma
     * cuenta no pise este cambio (y viceversa).
     */
    @Transactional
    public void registrarIntentoExitoso(String username) {
        usuarioRepository.findByUsernameIgnoreCaseConBloqueo(username).ifPresent(usuario -> {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        });
    }

    /** Consulta (sin modificar nada) hasta cuándo sigue bloqueada una cuenta, si lo está. */
    @Transactional(readOnly = true)
    public LocalDateTime obtenerBloqueadoHasta(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(Usuario::estaBloqueada)
                .map(Usuario::getBloqueadoHasta)
                .orElse(null);
    }

    /**
     * Se llama tras una contraseña incorrecta para una cuenta que sí existe. Si el bloqueo
     * anterior ya expiró, reinicia el conteo antes de sumar el intento (para no arrastrar
     * intentos viejos de una ventana ya cerrada). Si se alcanza el máximo, fija la fecha de
     * desbloqueo y la devuelve para que el mensaje al usuario indique cuánto falta.
     *
     * @return la fecha hasta la que queda bloqueada la cuenta, o null si no se bloqueó ahora.
     */
    @Transactional
    public LocalDateTime registrarIntentoFallido(String username) {
        return usuarioRepository.findByUsernameIgnoreCaseConBloqueo(username)
                .map(usuario -> {
                    if (usuario.getBloqueadoHasta() != null && !usuario.estaBloqueada()) {
                        // El bloqueo anterior ya venció: empezamos a contar de nuevo.
                        usuario.setIntentosFallidos(0);
                        usuario.setBloqueadoHasta(null);
                    }
                    usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
                    if (usuario.getIntentosFallidos() >= bloqueo.getMaxIntentos() && usuario.getBloqueadoHasta() == null) {
                        LocalDateTime hasta = LocalDateTime.now().plus(Duration.ofSeconds(bloqueo.getSegundosBloqueo()));
                        usuario.setBloqueadoHasta(hasta);
                        return hasta;
                    }
                    return null;
                })
                .orElse(null);
    }
}
