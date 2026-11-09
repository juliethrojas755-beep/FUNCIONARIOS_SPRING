package com.campuslands.c3.actividadenclase.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa a un funcionario y se guarda en la tabla funcionarios.
 */
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(length = 30)
    private String telefono;

    protected Funcionario() {
        // JPA necesita un constructor vacío para reconstruir los objetos desde MySQL.
    }

    public Funcionario(String nombre, String cargo, String departamento, String telefono) {
        this.nombre = nombre;
        this.cargo = cargo;
        this.departamento = departamento;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
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
