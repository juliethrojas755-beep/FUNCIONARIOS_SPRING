package com.campuslands.c3.actividadenclase;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campuslands.c3.actividadenclase.controller.FuncionarioController;
import com.campuslands.c3.actividadenclase.exception.FuncionarioNoEncontradoException;
import com.campuslands.c3.actividadenclase.model.Funcionario;
import com.campuslands.c3.actividadenclase.service.FuncionarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba la respuesta REST del controlador sin necesitar una base de datos real.
 */
@WebMvcTest(FuncionarioController.class)
class ActividadenclaseApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FuncionarioService funcionarioService;

    @Test
    void listarFuncionariosDevuelveLosRegistros() throws Exception {
        when(funcionarioService.listarTodos()).thenReturn(
                List.of(new Funcionario("Ana Gómez", "Analista", "Contabilidad", "3001234567")));

        mockMvc.perform(get("/api/v1/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana Gómez"))
                .andExpect(jsonPath("$[0].departamento").value("Contabilidad"));

        verify(funcionarioService).listarTodos();
    }

    @Test
    void consultarIdInexistenteDevuelve404() throws Exception {
        when(funcionarioService.buscarPorId(999L))
                .thenThrow(new FuncionarioNoEncontradoException(999L));

        mockMvc.perform(get("/api/v1/funcionarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe un funcionario con el ID 999."));
    }

    @Test
    void crearSinCamposObligatoriosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/funcionarios")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "cargo": "Auxiliar",
                                  "departamento": "Contabilidad"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("El nombre es obligatorio."));
    }

    @Test
    void listarDirectorioVacioDevuelve204SinCuerpo() throws Exception {
        when(funcionarioService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/funcionarios"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void buscarPriorizaNombreSobreDepartamento() throws Exception {
        when(funcionarioService.buscarPorNombre("ana"))
                .thenReturn(List.of(new Funcionario("Ana Gómez", "Analista", "Contabilidad", null)));

        mockMvc.perform(get("/api/v1/funcionarios")
                        .param("nombre", "ana")
                        .param("departamento", "Talento Humano"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana Gómez"));

        verify(funcionarioService).buscarPorNombre("ana");
    }

    @Test
    void filtroSinCoincidenciasDevuelve204SinCuerpo() throws Exception {
        when(funcionarioService.buscarPorDepartamento("Finanzas")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/funcionarios").param("departamento", "Finanzas"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void registrarConIdNoNuloDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/funcionarios")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 8,
                                  "nombre": "Ana Gómez",
                                  "cargo": "Analista",
                                  "departamento": "Contabilidad"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }
}
