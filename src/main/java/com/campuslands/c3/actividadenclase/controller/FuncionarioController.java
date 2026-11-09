package com.campuslands.c3.actividadenclase.controller;

import com.campuslands.c3.actividadenclase.dto.FuncionarioRequest;
import com.campuslands.c3.actividadenclase.exception.SolicitudInvalidaException;
import com.campuslands.c3.actividadenclase.model.Funcionario;
import com.campuslands.c3.actividadenclase.service.FuncionarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Expone las operaciones REST del directorio bajo /api/v1/funcionarios.
 */
@RestController
@RequestMapping("/api/v1/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public ResponseEntity<List<Funcionario>> listarTodos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String departamento) {
        List<Funcionario> funcionarios;
        if (nombre != null && !nombre.isBlank()) {
            funcionarios = funcionarioService.buscarPorNombre(nombre);
        } else if (departamento != null && !departamento.isBlank()) {
            funcionarios = funcionarioService.buscarPorDepartamento(departamento);
        } else {
            funcionarios = funcionarioService.listarTodos();
        }

        if (funcionarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping("/{id}")
    public Funcionario buscarPorId(@PathVariable Long id) {
        return funcionarioService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Funcionario> crear(@Valid @RequestBody FuncionarioRequest solicitud) {
        if (solicitud.getId() != null) {
            throw new SolicitudInvalidaException("El ID debe omitirse o ser nulo al registrar.");
        }
        Funcionario creado = funcionarioService.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public Funcionario actualizar(
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioRequest solicitud) {
        return funcionarioService.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        funcionarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
