package com.campuslands.c3.actividadenclase.security;

import com.campuslands.c3.actividadenclase.model.Usuario;
import com.campuslands.c3.actividadenclase.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Le dice a Spring Security de dónde salen las credenciales: la tabla "usuarios" en MySQL.
 * Si la cuenta tiene un bloqueo vigente, se marca como "no bloqueada = false" para que
 * Spring Security rechace el intento con LockedException sin siquiera comparar la contraseña.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña incorrectos."));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USUARIO")))
                .accountLocked(usuario.estaBloqueada())
                .build();
    }
}
