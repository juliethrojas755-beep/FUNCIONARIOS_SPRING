package com.campuslands.c3.actividadenclase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que llegan del formulario de registro. La validación de formato de la contraseña
 * (longitud, mayúsculas, etc.) se hace aparte en UsuarioService porque sus reglas son
 * configurables desde application.properties.
 */
public class RegistroRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "El nombre de usuario solo puede tener letras, números, puntos, guiones y guion bajo.")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;

    public RegistroRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
