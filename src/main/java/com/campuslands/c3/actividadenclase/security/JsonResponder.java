package com.campuslands.c3.actividadenclase.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;

/**
 * Escribe respuestas JSON muy simples (pares clave/valor) a mano, sin depender de
 * com.fasterxml.jackson.databind como dependencia directa de compilación: estas clases de
 * seguridad corren fuera del ciclo normal de Spring MVC (son manejadores de Spring Security),
 * así que no pasan por los conversores JSON configurados automáticamente para los controladores.
 */
final class JsonResponder {

    private JsonResponder() {
    }

    static void escribir(HttpServletResponse response, int estado, String clave, String valor) throws IOException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put(clave, valor);
        escribir(response, estado, cuerpo);
    }

    static void escribir(HttpServletResponse response, int estado, Map<String, Object> cuerpo) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(aJson(cuerpo));
    }

    private static String aJson(Map<String, Object> cuerpo) {
        StringBuilder json = new StringBuilder("{");
        boolean primero = true;
        for (Map.Entry<String, Object> entrada : cuerpo.entrySet()) {
            if (!primero) {
                json.append(',');
            }
            primero = false;
            json.append('"').append(escapar(entrada.getKey())).append("\":");
            Object valor = entrada.getValue();
            if (valor instanceof Boolean || valor instanceof Number) {
                json.append(valor);
            } else {
                json.append('"').append(escapar(String.valueOf(valor))).append('"');
            }
        }
        return json.append('}').toString();
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
