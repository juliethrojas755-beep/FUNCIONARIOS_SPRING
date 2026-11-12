package com.campuslands.c3.actividadenclase.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Reglas de bloqueo por intentos fallidos (prefijo app.lockout), configurables desde
 * application.properties o variables de entorno.
 */
@ConfigurationProperties(prefix = "app.lockout")
public class LockoutProperties {

    /** Intentos fallidos consecutivos permitidos antes de bloquear la cuenta. */
    private int maxIntentos = 5;

    /** Minutos que dura el bloqueo una vez se alcanza el máximo de intentos. */
    private int minutosBloqueo = 15;

    /** Intentos fallidos permitidos por dirección IP en la ventana de tiempo, sin importar la cuenta. */
    private int maxIntentosPorIp = 20;

    /** Minutos de la ventana de tiempo para el límite por IP. */
    private int minutosVentanaIp = 15;

    public int getMaxIntentos() {
        return maxIntentos;
    }

    public void setMaxIntentos(int maxIntentos) {
        this.maxIntentos = maxIntentos;
    }

    public int getMinutosBloqueo() {
        return minutosBloqueo;
    }

    public void setMinutosBloqueo(int minutosBloqueo) {
        this.minutosBloqueo = minutosBloqueo;
    }

    public int getMaxIntentosPorIp() {
        return maxIntentosPorIp;
    }

    public void setMaxIntentosPorIp(int maxIntentosPorIp) {
        this.maxIntentosPorIp = maxIntentosPorIp;
    }

    public int getMinutosVentanaIp() {
        return minutosVentanaIp;
    }

    public void setMinutosVentanaIp(int minutosVentanaIp) {
        this.minutosVentanaIp = minutosVentanaIp;
    }
}
