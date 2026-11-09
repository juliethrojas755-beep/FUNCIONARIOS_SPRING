package com.campuslands.c3.actividadenclase.exception;

/**
 * Se usa cuando la solicitud incumple una regla de entrada del taller.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
