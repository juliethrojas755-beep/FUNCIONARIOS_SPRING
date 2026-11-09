package com.campuslands.c3.actividadenclase.repository;

import com.campuslands.c3.actividadenclase.model.Funcionario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accede a los funcionarios en MySQL y declara las búsquedas que necesita el taller.
 */
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByNombreContainingIgnoreCase(String fragmento);

    List<Funcionario> findByDepartamentoIgnoreCase(String departamento);
}
