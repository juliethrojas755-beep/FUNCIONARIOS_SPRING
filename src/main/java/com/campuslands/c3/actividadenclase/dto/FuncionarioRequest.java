package com.campuslands.c3.actividadenclase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Define los campos que el cliente puede enviar al crear o editar un funcionario.
 */
public class FuncionarioRequest {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
    private String nombre;

    @NotBlank(message = "El cargo es obligatorio.")
    @Size(max = 100, message = "El cargo no puede superar 100 caracteres.")
    private String cargo;

    @NotBlank(message = "El departamento es obligatorio.")
    @Size(max = 100, message = "El departamento no puede superar 100 caracteres.")
    private String departamento;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres.")
    private String telefono;

    public FuncionarioRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
