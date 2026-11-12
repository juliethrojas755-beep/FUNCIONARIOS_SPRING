package com.campuslands.c3.actividadenclase.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

/**
 * Cuenta de acceso al directorio. La contraseña nunca se guarda en texto plano:
 * solo se guarda su hash BCrypt (ver UsuarioService).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    /** Null = cuenta no bloqueada. Si tiene fecha futura, la cuenta está bloqueada hasta ese momento. */
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    /** Control de concurrencia optimista para evitar perder incrementos de intentos simultáneos. */
    @Version
    private Long version;

    protected Usuario() {
        // JPA necesita un constructor vacío.
    }

    public Usuario(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.creadoEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    /** true si en este momento la cuenta sigue bloqueada. */
    public boolean estaBloqueada() {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(LocalDateTime.now());
    }
}
