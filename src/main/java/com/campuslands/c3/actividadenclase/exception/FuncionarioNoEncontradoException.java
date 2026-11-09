package com.campuslands.c3.actividadenclase.exception;

/**
 * Se usa cuando se solicita un funcionario cuyo ID no está registrado.
 */
public class FuncionarioNoEncontradoException extends RuntimeException {

    public FuncionarioNoEncontradoException(Long id) {
        super("No existe un funcionario con el ID " + id + ".");
    }
}
