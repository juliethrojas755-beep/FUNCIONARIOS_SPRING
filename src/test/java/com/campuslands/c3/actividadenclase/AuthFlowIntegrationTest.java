package com.campuslands.c3.actividadenclase;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

/**
 * Prueba el flujo completo de registro, login, acceso protegido, bloqueo por intentos
 * fallidos y logout, contra una base de datos H2 en memoria (ver application-test.properties)
 * y el filtro de seguridad real de la aplicación (no es un @WebMvcTest aislado).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void construirMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /** Arma el JSON del registro (los valores de prueba no tienen comillas ni caracteres especiales de JSON). */
    private static String jsonRegistro(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private void registrar(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro(username, password)))
                .andExpect(status().isCreated());
    }

    @Test
    void registrarUsuarioNuevoDevuelve201() throws Exception {
        registrar("carlos", "Clave#2026");
    }

    @Test
    void registrarConContrasenaDebilDevuelve400() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro("debil1", "clave123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsernameDuplicadoDevuelve409() throws Exception {
        registrar("duplicado", "Clave#2026");

        mockMvc.perform(post("/api/auth/registro")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro("duplicado", "OtraClave#1")))
                .andExpect(status().isConflict());
    }

    @Test
    void accederApiSinSesionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/funcionarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginInvalidoDevuelveMensajeGenerico() throws Exception {
        registrar("maria", "Clave#2026");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "maria")
                        .param("password", "ContrasenaIncorrecta1!"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void loginConUsuarioInexistenteDevuelveElMismoMensajeGenerico() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "no-existe-nadie")
                        .param("password", "loQueSea1!"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos."));
    }

    @Test
    void loginValidoPermiteAccederALaApiProtegidaYLuegoCerrarSesion() throws Exception {
        registrar("pedro", "Clave#2026");

        MvcResult resultadoLogin = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "pedro")
                        .param("password", "Clave#2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("pedro"))
                .andReturn();

        MockHttpSession sesion = (MockHttpSession) resultadoLogin.getRequest().getSession(false);

        // Con la sesión ya iniciada, la API del directorio responde (vacía -> 204).
        mockMvc.perform(get("/api/v1/funcionarios").session(sesion))
                .andExpect(status().isNoContent());

        // Cerrar sesión invalida la sesión del servidor.
        mockMvc.perform(post("/api/auth/logout").with(csrf()).session(sesion))
                .andExpect(status().isOk());

        // La misma sesión ya no sirve para entrar al directorio.
        mockMvc.perform(get("/api/v1/funcionarios").session(sesion))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void trasElMaximoDeIntentosFallidosLaCuentaQuedaBloqueada() throws Exception {
        registrar("sofia", "Clave#2026");
        // application-test.properties configura app.lockout.max-intentos=3

        for (int intento = 1; intento <= 2; intento++) {
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .param("username", "sofia")
                            .param("password", "Incorrecta" + intento + "!"))
                    .andExpect(status().isUnauthorized());
        }

        // El tercer intento fallido alcanza el máximo configurado y bloquea la cuenta.
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "sofia")
                        .param("password", "IncorrectaFinal!"))
                .andExpect(status().is(423))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Demasiados intentos fallidos")))
                .andExpect(jsonPath("$.segundosRestantes").value(org.hamcrest.Matchers.lessThanOrEqualTo(30)));

        // Aunque ahora escriba la contraseña correcta, la cuenta sigue bloqueada.
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", "sofia")
                        .param("password", "Clave#2026"))
                .andExpect(status().is(423));
    }
}
