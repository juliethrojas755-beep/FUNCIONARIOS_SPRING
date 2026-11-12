package com.campuslands.c3.actividadenclase.security;

import com.campuslands.c3.actividadenclase.service.UsuarioService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Reúne toda la configuración de seguridad: qué rutas necesitan sesión, cómo se procesan
 * login/logout, el manejo de CSRF con cookie (para que app.js y login.js puedan leerla) y
 * el límite de intentos por IP.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({PasswordPolicyProperties.class, LockoutProperties.class})
public class SecurityConfig {

    private static final String RUTA_LOGIN = "/api/auth/login";
    private static final String RUTA_LOGOUT = "/api/auth/logout";

    /**
     * Identifica las peticiones a la API (para responder 401 en JSON en vez de redirigir a
     * login.html). No se usa AntPathRequestMatcher porque Spring Security 7 lo quitó a favor
     * de PathPatternRequestMatcher; un RequestMatcher simple evita depender de esa API nueva.
     */
    private static final RequestMatcher PETICION_A_LA_API =
            request -> request.getRequestURI().startsWith(request.getContextPath() + "/api/");

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // Siempre comparar el hash (aunque el usuario no exista) para que el tiempo de
        // respuesta no delate si una cuenta está registrada o no.
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            UsuarioService usuarioService,
            IpAttemptTracker ipAttemptTracker) throws Exception {

        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();

        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(this::configurarAutorizacion)
                .csrf(csrf -> csrf
                        // El token viaja en una cookie legible por JavaScript (no HttpOnly) para
                        // que el frontend la lea y la devuelva en el encabezado X-XSRF-TOKEN.
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfRequestHandler))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(
                        new LoginRateLimitFilter(RUTA_LOGIN, ipAttemptTracker),
                        UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(login -> login
                        .loginPage("/login.html")
                        .loginProcessingUrl(RUTA_LOGIN)
                        .successHandler(new JsonAuthenticationSuccessHandler(usuarioService))
                        .failureHandler(new JsonAuthenticationFailureHandler(usuarioService, ipAttemptTracker))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl(RUTA_LOGOUT)
                        .logoutSuccessHandler(new JsonLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(manejo -> manejo
                        .defaultAuthenticationEntryPointFor(
                                new ApiAuthenticationEntryPoint(),
                                PETICION_A_LA_API));
        // Para rutas que no son /api/** (las páginas HTML), se mantiene el comportamiento por
        // defecto de formLogin: redirigir a /login.html si no hay sesión.

        return http.build();
    }

    private void configurarAutorizacion(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registro) {
        registro
                // Recursos públicos: la página de login/registro y lo que necesita para pintarse.
                .requestMatchers(
                        "/login.html", "/login.js", "/styles.css", "/favicon.ico",
                        "/api/auth/registro", "/api/auth/login", "/api/auth/me")
                .permitAll()
                // El directorio de funcionarios (página y API) exige sesión iniciada.
                .requestMatchers("/api/v1/funcionarios/**").authenticated()
                .anyRequest().authenticated();
    }
}
