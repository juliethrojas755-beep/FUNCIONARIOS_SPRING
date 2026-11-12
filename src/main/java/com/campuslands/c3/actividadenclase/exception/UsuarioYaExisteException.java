package com.campuslands.c3.actividadenclase.exception;

/** Se usa cuando alguien intenta registrarse con un nombre de usuario que ya existe. */
public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String mensaje) {
        super(mensaje);
    }
}
