package com.campuslands.c3.actividadenclase.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Requisitos de contraseña, configurables desde application.properties (prefijo app.password-policy)
 * o variables de entorno (APP_PASSWORD-POLICY_*). El máximo no debería pasar de 72: BCrypt
 * ignora en silencio los caracteres que sobren de esa longitud.
 */
@ConfigurationProperties(prefix = "app.password-policy")
public class PasswordPolicyProperties {

    private int longitudMinima = 8;
    private int longitudMaxima = 72;
    private boolean requiereMayuscula = true;
    private boolean requiereMinuscula = true;
    private boolean requiereNumero = true;
    private boolean requiereCaracterEspecial = true;

    public int getLongitudMinima() {
        return longitudMinima;
    }

    public void setLongitudMinima(int longitudMinima) {
        this.longitudMinima = longitudMinima;
    }

    public int getLongitudMaxima() {
        return longitudMaxima;
    }

    public void setLongitudMaxima(int longitudMaxima) {
        this.longitudMaxima = longitudMaxima;
    }

    public boolean isRequiereMayuscula() {
        return requiereMayuscula;
    }

    public void setRequiereMayuscula(boolean requiereMayuscula) {
        this.requiereMayuscula = requiereMayuscula;
    }

    public boolean isRequiereMinuscula() {
        return requiereMinuscula;
    }

    public void setRequiereMinuscula(boolean requiereMinuscula) {
        this.requiereMinuscula = requiereMinuscula;
    }

    public boolean isRequiereNumero() {
        return requiereNumero;
    }

    public void setRequiereNumero(boolean requiereNumero) {
        this.requiereNumero = requiereNumero;
    }

    public boolean isRequiereCaracterEspecial() {
        return requiereCaracterEspecial;
    }

    public void setRequiereCaracterEspecial(boolean requiereCaracterEspecial) {
        this.requiereCaracterEspecial = requiereCaracterEspecial;
    }

    /** Texto para mostrar al usuario en el formulario y en los mensajes de error. */
    public String describirRequisitos() {
        StringBuilder texto = new StringBuilder("Entre ")
                .append(longitudMinima).append(" y ").append(longitudMaxima).append(" caracteres");
        if (requiereMayuscula) texto.append(", al menos una mayúscula");
        if (requiereMinuscula) texto.append(", al menos una minúscula");
        if (requiereNumero) texto.append(", al menos un número");
        if (requiereCaracterEspecial) texto.append(", al menos un carácter especial (!@#$...)");
        texto.append(".");
        return texto.toString();
    }
}
