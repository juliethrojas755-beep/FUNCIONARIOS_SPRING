package com.campuslands.c3.actividadenclase.service;

import com.campuslands.c3.actividadenclase.dto.FuncionarioRequest;
import com.campuslands.c3.actividadenclase.exception.FuncionarioNoEncontradoException;
import com.campuslands.c3.actividadenclase.model.Funcionario;
import com.campuslands.c3.actividadenclase.repository.FuncionarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contiene las reglas del directorio y coordina las operaciones del repositorio.
 */
@Service
@Transactional
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Funcionario buscarPorId(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new FuncionarioNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public List<Funcionario> buscarPorNombre(String fragmento) {
        return funcionarioRepository.findByNombreContainingIgnoreCase(fragmento.trim());
    }

    @Transactional(readOnly = true)
    public List<Funcionario> buscarPorDepartamento(String departamento) {
        return funcionarioRepository.findByDepartamentoIgnoreCase(departamento.trim());
    }

    public Funcionario crear(FuncionarioRequest solicitud) {
        Funcionario funcionario = new Funcionario(
                solicitud.getNombre().trim(),
                solicitud.getCargo().trim(),
                solicitud.getDepartamento().trim(),
                limpiarTelefono(solicitud.getTelefono()));
        return funcionarioRepository.save(funcionario);
    }

    public Funcionario actualizar(Long id, FuncionarioRequest solicitud) {
        Funcionario funcionario = buscarPorId(id);
        funcionario.setNombre(solicitud.getNombre().trim());
        funcionario.setCargo(solicitud.getCargo().trim());
        funcionario.setDepartamento(solicitud.getDepartamento().trim());
        funcionario.setTelefono(limpiarTelefono(solicitud.getTelefono()));
        return funcionarioRepository.save(funcionario);
    }

    public void eliminar(Long id) {
        Funcionario funcionario = buscarPorId(id);
        funcionarioRepository.delete(funcionario);
    }

    private String limpiarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        return telefono.trim();
    }
}
