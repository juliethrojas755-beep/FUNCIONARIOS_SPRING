"use strict";

const API_URL = "/api/v1/funcionarios";
const METODOS_QUE_CAMBIAN_DATOS = new Set(["POST", "PUT", "DELETE", "PATCH"]);

const formulario = document.querySelector("#formulario-funcionario");
const filasFuncionarios = document.querySelector("#filas-funcionarios");
const mensaje = document.querySelector("#mensaje");
const tituloFormulario = document.querySelector("#titulo-formulario");
const estadoFormulario = document.querySelector("#estado-formulario");
const botonGuardar = document.querySelector("#guardar");
const usuarioActualEtiqueta = document.querySelector("#usuario-actual");
const botonCerrarSesion = document.querySelector("#cerrar-sesion");

let idEnEdicion = null;

/** Lee la cookie XSRF-TOKEN que pone Spring Security para devolverla en el encabezado. */
function leerTokenCsrf() {
    const coincidencia = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return coincidencia ? decodeURIComponent(coincidencia[1]) : null;
}

/**
 * Pide datos al backend y convierte las respuestas HTTP de error en mensajes útiles.
 * Si la sesión venció (401/403), manda a la persona a iniciar sesión de nuevo.
 */
async function solicitar(ruta, opciones = {}) {
    const metodo = (opciones.method ?? "GET").toUpperCase();
    const cabeceras = { ...opciones.headers };
    if (METODOS_QUE_CAMBIAN_DATOS.has(metodo)) {
        cabeceras["Content-Type"] = cabeceras["Content-Type"] ?? "application/json";
        cabeceras["X-XSRF-TOKEN"] = leerTokenCsrf() ?? "";
    }

    const respuesta = await fetch(ruta, { ...opciones, headers: cabeceras });

    if (respuesta.status === 401 || respuesta.status === 403) {
        window.location.href = "/login.html";
        throw new Error("Tu sesión no es válida o venció.");
    }

    if (!respuesta.ok) {
        let detalle = `La solicitud falló (HTTP ${respuesta.status}).`;
        try {
            const error = await respuesta.json();
            if (error.mensaje) {
                detalle = error.mensaje;
            }
        } catch {
            // Algunas respuestas de error no incluyen un cuerpo JSON.
        }
        throw new Error(detalle);
    }

    if (respuesta.status === 204) {
        return null;
    }
    return respuesta.json();
}

/**
 * Muestra un mensaje informativo o de error debajo del título de la tabla.
 */
function mostrarMensaje(texto, esError = false) {
    mensaje.textContent = texto;
    mensaje.classList.toggle("error", esError);
}

/**
 * Agrega texto a una celda sin interpretar los datos como HTML.
 */
function crearCelda(valor) {
    const celda = document.createElement("td");
    celda.textContent = valor ?? "";
    return celda;
}

/**
 * Dibuja la tabla; si la API devuelve una lista vacía, deja solo el mensaje de vacío.
 */
function mostrarFuncionarios(funcionarios) {
    filasFuncionarios.replaceChildren();

    if (funcionarios.length === 0) {
        const fila = document.createElement("tr");
        fila.className = "fila-vacia";
        const celda = document.createElement("td");
        celda.colSpan = 6;
        celda.textContent = "No se encontraron funcionarios para esta búsqueda.";
        fila.append(celda);
        filasFuncionarios.append(fila);
        return;
    }

    funcionarios.forEach((funcionario) => {
        const fila = document.createElement("tr");
        fila.append(
            crearCelda(funcionario.id),
            crearCelda(funcionario.nombre),
            crearCelda(funcionario.cargo),
            crearCelda(funcionario.departamento),
            crearCelda(funcionario.telefono || "—")
        );

        const celdaAcciones = document.createElement("td");
        const acciones = document.createElement("div");
        acciones.className = "acciones-tabla";

        const botonEditar = document.createElement("button");
        botonEditar.type = "button";
        botonEditar.className = "boton boton-secundario boton-tabla";
        botonEditar.textContent = "Editar";
        botonEditar.addEventListener("click", () => prepararEdicion(funcionario));

        const botonEliminar = document.createElement("button");
        botonEliminar.type = "button";
        botonEliminar.className = "boton boton-tabla boton-eliminar";
        botonEliminar.textContent = "Eliminar";
        botonEliminar.addEventListener("click", () => eliminarFuncionario(funcionario));

        acciones.append(botonEditar, botonEliminar);
        celdaAcciones.append(acciones);
        fila.append(celdaAcciones);
        filasFuncionarios.append(fila);
    });
}

/**
 * Carga el listado completo y permite recuperarse de errores mostrando su causa.
 */
async function cargarTodos(mensajeExito = null) {
    try {
        const respuesta = await solicitar(API_URL);
        const funcionarios = respuesta ?? [];
        mostrarFuncionarios(funcionarios);
        mostrarMensaje(mensajeExito ?? (
            funcionarios.length === 0
                ? "El directorio está vacío. Registra un funcionario para comenzar."
                : `${funcionarios.length} funcionario(s) en el directorio.`
        ));
    } catch (error) {
        mostrarFuncionarios([]);
        mostrarMensaje(error.message, true);
    }
}

/**
 * Pone los datos elegidos en el formulario y conserva el ID solo para la edición.
 */
function prepararEdicion(funcionario) {
    idEnEdicion = funcionario.id;
    formulario.elements.nombre.value = funcionario.nombre;
    formulario.elements.cargo.value = funcionario.cargo;
    formulario.elements.departamento.value = funcionario.departamento;
    formulario.elements.telefono.value = funcionario.telefono ?? "";
    tituloFormulario.textContent = `Editar funcionario #${funcionario.id}`;
    estadoFormulario.textContent = `Editando ID ${funcionario.id}`;
    botonGuardar.textContent = "Guardar cambios";
    document.querySelector("#nombre").focus();
    window.scrollTo({ top: 0, behavior: "smooth" });
}

/**
 * Borra el registro solo después de que la persona confirme la acción.
 */
async function eliminarFuncionario(funcionario) {
    const confirmado = window.confirm(
        `¿Seguro que deseas eliminar a ${funcionario.nombre} (ID ${funcionario.id})?`
    );
    if (!confirmado) {
        return;
    }

    try {
        await solicitar(`${API_URL}/${funcionario.id}`, { method: "DELETE" });
        if (idEnEdicion === funcionario.id) {
            limpiarFormulario();
        }
        await cargarTodos(`Se eliminó a ${funcionario.nombre}.`);
    } catch (error) {
        mostrarMensaje(error.message, true);
    }
}

/**
 * Restablece el formulario para comenzar un registro nuevo.
 */
function limpiarFormulario() {
    formulario.reset();
    idEnEdicion = null;
    tituloFormulario.textContent = "Nuevo funcionario";
    estadoFormulario.textContent = "Creando";
    botonGuardar.textContent = "Guardar funcionario";
}

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const datos = {
        nombre: formulario.elements.nombre.value.trim(),
        cargo: formulario.elements.cargo.value.trim(),
        departamento: formulario.elements.departamento.value.trim(),
        telefono: formulario.elements.telefono.value.trim() || null
    };
    const editando = idEnEdicion !== null;

    try {
        const funcionario = await solicitar(
            editando ? `${API_URL}/${idEnEdicion}` : API_URL,
            {
                method: editando ? "PUT" : "POST",
                body: JSON.stringify(datos)
            }
        );
        const mensajeExito = editando
            ? `Se actualizaron los datos de ${funcionario.nombre}.`
            : `Se registró a ${funcionario.nombre}.`;
        limpiarFormulario();
        await cargarTodos(mensajeExito);
    } catch (error) {
        mostrarMensaje(error.message, true);
    }
});

document.querySelector("#limpiar").addEventListener("click", limpiarFormulario);
document.querySelector("#mostrar-todos").addEventListener("click", cargarTodos);

document.querySelector("#buscar-id").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const id = document.querySelector("#id-busqueda").value;
    try {
        const funcionario = await solicitar(`${API_URL}/${encodeURIComponent(id)}`);
        mostrarFuncionarios([funcionario]);
        mostrarMensaje("Resultado de la búsqueda por ID.");
    } catch (error) {
        mostrarFuncionarios([]);
        mostrarMensaje(error.message, true);
    }
});

document.querySelector("#buscar-nombre").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const fragmento = document.querySelector("#nombre-busqueda").value.trim();
    try {
        const resultado = await solicitar(`${API_URL}?${new URLSearchParams({ nombre: fragmento })}`);
        const funcionarios = resultado ?? [];
        mostrarFuncionarios(funcionarios);
        mostrarMensaje(funcionarios.length === 0
            ? `No se encontraron funcionarios cuyo nombre contenga "${fragmento}".`
            : `Resultados para el nombre "${fragmento}".`);
    } catch (error) {
        mostrarFuncionarios([]);
        mostrarMensaje(error.message, true);
    }
});

document.querySelector("#buscar-departamento").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const nombre = document.querySelector("#departamento-busqueda").value.trim();
    try {
        const resultado = await solicitar(`${API_URL}?${new URLSearchParams({ departamento: nombre })}`);
        const funcionarios = resultado ?? [];
        mostrarFuncionarios(funcionarios);
        mostrarMensaje(funcionarios.length === 0
            ? `No se encontraron funcionarios en "${nombre}".`
            : `Funcionarios del departamento "${nombre}".`);
    } catch (error) {
        mostrarFuncionarios([]);
        mostrarMensaje(error.message, true);
    }
});

/** Pregunta quién inició sesión para mostrarlo en la barra de arriba. */
async function cargarUsuarioActual() {
    try {
        const respuesta = await fetch("/api/auth/me");
        const datos = await respuesta.json();
        if (datos.autenticado) {
            usuarioActualEtiqueta.textContent = `Conectado como ${datos.username}`;
        } else {
            window.location.href = "/login.html";
        }
    } catch {
        // Si no se puede confirmar la sesión, se deja que las llamadas a la API lo resuelvan.
    }
}

botonCerrarSesion.addEventListener("click", async () => {
    try {
        await fetch("/api/auth/logout", {
            method: "POST",
            headers: { "X-XSRF-TOKEN": leerTokenCsrf() ?? "" }
        });
    } finally {
        window.location.href = "/login.html";
    }
});

cargarUsuarioActual();
cargarTodos();
