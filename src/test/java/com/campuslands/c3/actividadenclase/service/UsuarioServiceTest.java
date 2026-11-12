package com.campuslands.c3.actividadenclase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.campuslands.c3.actividadenclase.dto.RegistroRequest;
import com.campuslands.c3.actividadenclase.exception.SolicitudInvalidaException;
import com.campuslands.c3.actividadenclase.exception.UsuarioYaExisteException;
import com.campuslands.c3.actividadenclase.model.Usuario;
import com.campuslands.c3.actividadenclase.repository.UsuarioRepository;
import com.campuslands.c3.actividadenclase.security.LockoutProperties;
import com.campuslands.c3.actividadenclase.security.PasswordPolicyProperties;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pruebas unitarias de las reglas de negocio: no levantan Spring ni tocan una base de
 * datos real, así que son rápidas y no dependen de que MySQL esté encendido.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UsuarioService usuarioService;
    private LockoutProperties lockoutProperties;

    @BeforeEach
    void prepararServicio() {
        PasswordPolicyProperties politica = new PasswordPolicyProperties();
        lockoutProperties = new LockoutProperties();
        lockoutProperties.setMaxIntentos(3);
        lockoutProperties.setSegundosBloqueo(30);
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, politica, lockoutProperties);
    }

    private RegistroRequest solicitud(String username, String password) {
        RegistroRequest solicitud = new RegistroRequest();
        solicitud.setUsername(username);
        solicitud.setPassword(password);
        return solicitud;
    }

    @Test
    void registrarConContrasenaValidaCreaLaCuenta() {
        when(usuarioRepository.existsByUsernameIgnoreCase("ana")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Usuario creado = usuarioService.registrar(solicitud("ana", "Clave#2026"));

        assertThat(creado.getUsername()).isEqualTo("ana");
        assertThat(passwordEncoder.matches("Clave#2026", creado.getPasswordHash())).isTrue();
    }

    @Test
    void registrarConContrasenaDebilRechazaElRegistro() {
        assertThatThrownBy(() -> usuarioService.registrar(solicitud("ana", "clave123")))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void registrarConUsuarioRepetidoRechazaElRegistro() {
        when(usuarioRepository.existsByUsernameIgnoreCase("ana")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(solicitud("ana", "Clave#2026")))
                .isInstanceOf(UsuarioYaExisteException.class);
    }

    @Test
    void intentoFallidoAumentaElContadorSinBloquearAntesDelLimite() {
        Usuario usuario = new Usuario("ana", "hash");
        usuario.setIntentosFallidos(1);
        when(usuarioRepository.findByUsernameIgnoreCaseConBloqueo("ana")).thenReturn(Optional.of(usuario));

        LocalDateTime bloqueadoHasta = usuarioService.registrarIntentoFallido("ana");

        assertThat(bloqueadoHasta).isNull();
        assertThat(usuario.getIntentosFallidos()).isEqualTo(2);
    }

    @Test
    void alLlegarAlMaximoDeIntentosSeBloqueaLaCuenta() {
        Usuario usuario = new Usuario("ana", "hash");
        usuario.setIntentosFallidos(2); // el próximo intento llega al máximo configurado (3)
        when(usuarioRepository.findByUsernameIgnoreCaseConBloqueo("ana")).thenReturn(Optional.of(usuario));

        LocalDateTime bloqueadoHasta = usuarioService.registrarIntentoFallido("ana");

        assertThat(bloqueadoHasta).isNotNull();
        assertThat(usuario.getIntentosFallidos()).isEqualTo(3);
        assertThat(usuario.estaBloqueada()).isTrue();
    }

    @Test
    void siElBloqueoYaExpiroElSiguienteIntentoReinicioElContador() {
        Usuario usuario = new Usuario("ana", "hash");
        usuario.setIntentosFallidos(3);
        usuario.setBloqueadoHasta(LocalDateTime.now().minusMinutes(1)); // bloqueo ya vencido
        when(usuarioRepository.findByUsernameIgnoreCaseConBloqueo("ana")).thenReturn(Optional.of(usuario));

        LocalDateTime bloqueadoHasta = usuarioService.registrarIntentoFallido("ana");

        assertThat(bloqueadoHasta).isNull();
        assertThat(usuario.getIntentosFallidos()).isEqualTo(1);
        assertThat(usuario.getBloqueadoHasta()).isNull();
    }

    @Test
    void unInicioExitosoReiniciaElContadorYQuitaElBloqueo() {
        Usuario usuario = new Usuario("ana", "hash");
        usuario.setIntentosFallidos(2);
        usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
        when(usuarioRepository.findByUsernameIgnoreCaseConBloqueo("ana")).thenReturn(Optional.of(usuario));

        usuarioService.registrarIntentoExitoso("ana");

        assertThat(usuario.getIntentosFallidos()).isZero();
        assertThat(usuario.getBloqueadoHasta()).isNull();
    }
}
