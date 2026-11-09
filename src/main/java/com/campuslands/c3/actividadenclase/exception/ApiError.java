package com.campuslands.c3.actividadenclase.exception;

import java.time.LocalDateTime;

/**
 * Formato sencillo y común para describir errores de la API.
 */
public record ApiError(LocalDateTime fecha, int estado, String mensaje) {
}
