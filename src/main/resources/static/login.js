"use strict";

/* Página pública de acceso: inicia sesión o crea una cuenta contra la API real
   (nada de credenciales simuladas ni almacenadas en el navegador). */

const pestanaLogin = document.querySelector("#pestana-login");
const pestanaRegistro = document.querySelector("#pestana-registro");
const formularioLogin = document.querySelector("#formulario-login");
const formularioRegistro = document.querySelector("#formulario-registro");
const mensajeAcceso = document.querySelector("#mensaje-acceso");

/** Lee la cookie XSRF-TOKEN que pone Spring Security para poder mandarla de vuelta. */
function leerTokenCsrf() {
    const coincidencia = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return coincidencia ? decodeURIComponent(coincidencia[1]) : null;
}

function mostrarMensaje(texto, esError = true) {
    mensajeAcceso.textContent = texto;
    mensajeAcceso.classList.toggle("error", esError);
}

function limpiarMensaje() {
    mensajeAcceso.textContent = "";
    mensajeAcceso.classList.remove("error");
}

function mostrarPestana(pestanaAMostrar) {
    const esLogin = pestanaAMostrar === "login";
    pestanaLogin.classList.toggle("activa", esLogin);
    pestanaRegistro.classList.toggle("activa", !esLogin);
    pestanaLogin.setAttribute("aria-selected", String(esLogin));
    pestanaRegistro.setAttribute("aria-selected", String(!esLogin));
    formularioLogin.classList.toggle("oculto", !esLogin);
    formularioRegistro.classList.toggle("oculto", esLogin);
    limpiarMensaje();
}

pestanaLogin.addEventListener("click", () => mostrarPestana("login"));
pestanaRegistro.addEventListener("click", () => mostrarPestana("registro"));

/** Botón de mostrar/ocultar para cada campo de contraseña. */
document.querySelectorAll(".boton-ver-contrasena").forEach((boton) => {
    boton.addEventListener("click", () => {
        const campo = document.getElementById(boton.dataset.objetivo);
        const mostrando = campo.type === "text";
        campo.type = mostrando ? "password" : "text";
        boton.textContent = mostrando ? "Mostrar" : "Ocultar";
        boton.setAttribute("aria-pressed", String(!mostrando));
    });
});

formularioLogin.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarMensaje();

    const cuerpo = new URLSearchParams({
        username: formularioLogin.elements.username.value.trim(),
        password: formularioLogin.elements.password.value
    });

    try {
        const respuesta = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-XSRF-TOKEN": leerTokenCsrf() ?? "",
                "Accept": "application/json"
            },
            body: cuerpo
        });

        const datos = await respuesta.json().catch(() => ({}));

        if (respuesta.ok) {
            window.location.href = "/";
            return;
        }

        mostrarMensaje(datos.mensaje || "No se pudo iniciar sesión.");
    } catch {
        mostrarMensaje("No se pudo conectar con el servidor. Intenta de nuevo.");
    }
});

formularioRegistro.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limpiarMensaje();

    const username = formularioRegistro.elements.username.value.trim();
    const password = formularioRegistro.elements.password.value;
    const confirmacion = formularioRegistro.elements.passwordConfirmar.value;

    if (password !== confirmacion) {
        mostrarMensaje("Las dos contraseñas no coinciden.");
        return;
    }

    try {
        const respuesta = await fetch("/api/auth/registro", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-XSRF-TOKEN": leerTokenCsrf() ?? ""
            },
            body: JSON.stringify({ username, password })
        });

        const datos = await respuesta.json().catch(() => ({}));

        if (respuesta.ok) {
            formularioRegistro.reset();
            mostrarPestana("login");
            mostrarMensaje(`Cuenta creada para "${datos.username}". Ya puedes iniciar sesión.`, false);
            return;
        }

        mostrarMensaje(datos.mensaje || "No se pudo crear la cuenta.");
    } catch {
        mostrarMensaje("No se pudo conectar con el servidor. Intenta de nuevo.");
    }
});

// Pide la cookie CSRF apenas carga la página (una petición GET cualquiera la activa).
fetch("/api/auth/me").catch(() => {});
