package com.campuslands.c3.actividadenclase.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte errores conocidos en respuestas HTTP claras para la página o Postman.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(FuncionarioNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarFuncionarioNoEncontrado(
            FuncionarioNoEncontradoException excepcion) {
        return crearRespuesta(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ApiError> manejarSolicitudInvalida(SolicitudInvalidaException excepcion) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<ApiError> manejarUsuarioYaExiste(UsuarioYaExisteException excepcion) {
        return crearRespuesta(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException excepcion) {
        String mensaje = excepcion.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));
        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJsonInvalido(HttpMessageNotReadableException excepcion) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido.");
    }

    private ResponseEntity<ApiError> crearRespuesta(HttpStatus estado, String mensaje) {
        ApiError error = new ApiError(LocalDateTime.now(), estado.value(), mensaje);
        return ResponseEntity.status(estado).body(error);
    }
}
