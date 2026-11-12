package com.campuslands.c3.actividadenclase.repository;

import com.campuslands.c3.actividadenclase.model.Usuario;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Igual que findByUsernameIgnoreCase, pero bloquea la fila en MySQL (SELECT ... FOR UPDATE)
     * mientras dura la transacción. Se usa al registrar un intento fallido o exitoso para que,
     * si dos peticiones llegan casi al mismo tiempo para la misma cuenta, se procesen una tras
     * otra y el conteo de intentos no se pierda ni se duplique.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where lower(u.username) = lower(:username)")
    Optional<Usuario> findByUsernameIgnoreCaseConBloqueo(@Param("username") String username);
}
