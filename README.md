# Directorio de funcionarios

Aplicación de práctica construida con Java 21, Spring Boot MVC, Spring Data JPA y MySQL. La página usa HTML, CSS y JavaScript vanilla; Spring Boot sirve tanto la página como la API, así que no hay que instalar un servidor frontend ni configurar CORS.

## 1. Estructura del proyecto

```text
actividadenclase/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/campuslands/c3/actividadenclase/
    │   │   ├── ActividadenclaseApplication.java
    │   │   ├── controller/
    │   │   │   └── FuncionarioController.java
    │   │   ├── dto/
    │   │   │   └── FuncionarioRequest.java
    │   │   ├── exception/
    │   │   │   ├── ApiError.java
    │   │   │   ├── ApiExceptionHandler.java
    │       │   │   ├── FuncionarioNoEncontradoException.java
    │       │   │   └── SolicitudInvalidaException.java
    │   │   ├── model/
    │   │   │   └── Funcionario.java
    │   │   ├── repository/
    │   │   │   └── FuncionarioRepository.java
    │   │   └── service/
    │   │       └── FuncionarioService.java
    │   └── resources/
    │       ├── application.properties
    │       ├── db/
    │       │   └── funcionarios.sql
    │       └── static/
    │           ├── app.js
    │           ├── index.html
    │           └── styles.css
    └── test/
        └── java/com/campuslands/c3/actividadenclase/
            └── ActividadenclaseApplicationTests.java
```

## 2. Requisitos e instalación

1. **JDK 21:** instala una distribución como Eclipse Temurin 21, Oracle JDK 21 o la distribución recomendada por tu bootcamp. Instala el JDK, no solo el JRE.
2. **MySQL 8:** instala MySQL Server y, si quieres usar una interfaz gráfica, MySQL Workbench. Mantén el puerto usual `3306`.
3. **IDE:** IntelliJ IDEA Community o Visual Studio Code con Extension Pack for Java y Spring Boot Extension Pack. También puedes usar otro IDE con soporte Maven.
4. **Maven:** el proyecto ya incluye Maven Wrapper (`mvnw` y `mvnw.cmd`), que descarga y usa la versión de Maven indicada por el proyecto. No necesitas instalar Maven globalmente.
5. **Git:** se necesita para publicar el proyecto en GitHub. No hace falta instalarlo para ejecutar la aplicación.

### Comprobar Java y Maven

Abre una terminal nueva y ejecuta:

```text
java -version
```

La salida debe indicar `21`. En la raíz del proyecto comprueba Maven:

```text
# macOS/Linux
./mvnw -version

# Windows PowerShell o CMD
mvnw.cmd -version
```

La salida de Maven Wrapper también debe indicar que usa Java 21. Si dice Java 17, configura `JAVA_HOME` y abre una terminal nueva antes de continuar.

### Configurar `JAVA_HOME` y `PATH`

**Windows 10/11**

1. Busca la carpeta instalada del JDK, por ejemplo `C:\Program Files\Eclipse Adoptium\jdk-21.0.x`.
2. Abre Inicio y busca **Editar las variables de entorno del sistema**.
3. Pulsa **Variables de entorno**. En variables del sistema crea o edita `JAVA_HOME` y asigna la ruta de la carpeta del JDK (no agregues `\bin`).
4. Edita `Path` y agrega `%JAVA_HOME%\bin`.
5. Acepta las ventanas y abre una terminal nueva. Verifica con `echo %JAVA_HOME%` en CMD, `$env:JAVA_HOME` en PowerShell y `java -version`.

Como alternativa temporal en PowerShell, ajusta la ruta real y luego verifica:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.x"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

**macOS (zsh, shell habitual):** instala JDK 21 y ejecuta:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Para conservarlo al abrir terminales nuevas, agrega esas dos líneas al final de `~/.zshrc` y ejecuta `source ~/.zshrc`.

**Linux (bash):** primero identifica la ruta instalada con `ls /usr/lib/jvm`. Después usa la ruta de tu JDK 21, por ejemplo:

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Para hacerlo permanente, agrega las líneas correctas al final de `~/.bashrc` y ejecuta `source ~/.bashrc`. El nombre exacto de la carpeta puede variar según la distribución.

## 3. Encender MySQL y activar inicio automático

El servidor MySQL debe estar iniciado antes de ejecutar la aplicación.

- **Windows:** abre `services.msc`, busca el servicio `MySQL80` (el nombre puede variar), pulsa **Iniciar** y en sus propiedades selecciona tipo de inicio **Automático**. Para iniciar el servicio pueden requerirse permisos de administrador.
- **macOS con Homebrew:** ejecuta `brew services start mysql`. Comprueba con `brew services list`. Si instalaste MySQL con el instalador oficial, inicia MySQL desde System Settings o MySQL Preference Pane y habilita su opción de inicio automático.
- **Linux con systemd:** ejecuta `sudo systemctl enable --now mysql` (en algunas distribuciones el servicio se llama `mysqld`). Comprueba con `systemctl status mysql`.

El proyecto se conecta a `localhost:3306`; no necesitas abrir MySQL a otros computadores ni permitir conexiones externas.

## 4. Crear la base de datos, la tabla y el usuario

El script [funcionarios.sql](src/main/resources/db/funcionarios.sql) crea la base `directorio_funcionarios`, la tabla `funcionarios`, un usuario local de aplicación con permisos limitados a esa base y tres datos de ejemplo. Dos pertenecen a Contabilidad.

Desde la raíz del proyecto, abre una terminal y ejecuta el script con una cuenta administradora de MySQL:

```bash
# macOS/Linux
mysql -u root -p < src/main/resources/db/funcionarios.sql
```

En Windows, en CMD o PowerShell, desde la raíz del proyecto:

```powershell
mysql -u root -p < src\main\resources\db\funcionarios.sql
```

Escribe la contraseña de administrador de MySQL cuando la pida. Si el comando `mysql` no se reconoce, usa la terminal incluida en MySQL o agrega la carpeta `bin` de MySQL al `PATH`. También puedes abrir el script en MySQL Workbench y ejecutarlo conectado como administrador.

El script crea `directorio_app` con la contraseña de ejemplo `CambiaEstaClave123!` y le da solamente permisos `SELECT`, `INSERT`, `UPDATE` y `DELETE` en esta base. **Cambia esa contraseña de ejemplo** en el script antes de ejecutarlo, o cambia después la contraseña del usuario; debe coincidir con `DB_PASSWORD` en la configuración de la aplicación. No uses la contraseña de ejemplo en un entorno real.

Para cambiarla desde MySQL, conectado como administrador:

```sql
ALTER USER 'directorio_app'@'localhost' IDENTIFIED BY 'tu_clave_nueva';
```

Si prefieres crear el usuario manualmente, ejecuta como administrador:

```sql
CREATE USER 'directorio_app'@'localhost' IDENTIFIED BY 'tu_clave_nueva';
GRANT SELECT, INSERT, UPDATE, DELETE
    ON directorio_funcionarios.*
    TO 'directorio_app'@'localhost';
```

En ese caso, omite del script las sentencias `CREATE USER` y `GRANT` o ejecútalo antes de crearlas para evitar que la contraseña no coincida.

## 5. Configurar la conexión

La configuración está en [application.properties](src/main/resources/application.properties):

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/directorio_funcionarios?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USER:directorio_app}
spring.datasource.password=${DB_PASSWORD:CambiaEstaClave123!}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
server.port=${SERVER_PORT:8080}
```

Lo que aparece después de `:` es el valor predeterminado. Si cambiaste usuario o clave, cambia los valores de `DB_USER` y `DB_PASSWORD` en el archivo, o configura variables de entorno. No compartas ni subas contraseñas reales al control de versiones.

Ejemplo temporal para la terminal:

```bash
# macOS/Linux
export DB_USER=directorio_app
export DB_PASSWORD='tu_clave_nueva'
```

```powershell
# Windows PowerShell
$env:DB_USER = "directorio_app"
$env:DB_PASSWORD = "tu_clave_nueva"
```

`ddl-auto=validate` significa que Hibernate revisa la tabla existente, pero **no crea ni borra** la tabla. Ejecuta el script SQL antes de iniciar la aplicación.

## 6. Abrir y ejecutar en el IDE

1. Abre en el IDE la carpeta que contiene `pom.xml`, no solamente `src`.
2. Espera a que el IDE importe el proyecto Maven y descargue dependencias. Se necesita internet la primera vez.
3. Verifica que el SDK del proyecto sea JDK 21. En IntelliJ revisa **Project Structure → Project SDK**. En VS Code, selecciona el JDK 21 si la extensión de Java lo solicita.
4. No hace falta activar **annotation processing**: el código no usa Lombok ni procesadores de anotaciones.
5. Inicia MySQL y ejecuta el script SQL primero.
6. Ejecuta la clase `ActividadenclaseApplication` desde el IDE, o abre una terminal en la raíz y ejecuta:

```bash
# macOS/Linux
./mvnw spring-boot:run
```

```powershell
# Windows
mvnw.cmd spring-boot:run
```

Espera el mensaje de Spring Boot que indica que la aplicación se inició. Abre **http://localhost:8080/** en el navegador. La página y la API se sirven desde el mismo puerto.

Si el IDE pregunta si Java puede aceptar conexiones en el firewall, para este taller local no hace falta exponer el servidor a internet. Si necesitas acceder desde otros dispositivos de tu red, permite solo redes privadas/de confianza; no habilites redes públicas. Para usarlo únicamente en tu computador, rechaza el acceso externo.

### Puerto 8080 ocupado

El error `Port 8080 was already in use` significa que otro programa ya usa ese puerto. Cambia `server.port` a `8081` en `application.properties`, o define `SERVER_PORT=8081`. Reinicia y abre **http://localhost:8081/**. Si cambias el puerto en el navegador, usa también ese puerto al probar la API.

## 7. Crear el repositorio del proyecto en GitHub

Git guarda versiones del proyecto; GitHub aloja una copia en internet para entregar el taller. Si no está instalado, instala Git desde [git-scm.com](https://git-scm.com/) (en macOS también puedes instalar las herramientas de línea de comandos de Xcode). Verifica con:

```bash
git --version
```

Primero, desde la carpeta del proyecto, revisa si ya estás dentro de otro repositorio:

```bash
git rev-parse --show-toplevel
git remote -v
```

Si la primera instrucción muestra una carpeta superior al proyecto, **no ejecutes `git init` dentro de esta carpeta**: crearías un repositorio anidado. Puedes entregar el proyecto en el repositorio superior si esa es la opción del bootcamp. Para crear uno independiente:

1. Entra a [github.com](https://github.com/), inicia sesión y pulsa **New repository**.
2. Escribe `directorio-funcionarios` como nombre. Elige la visibilidad que te pida el instructor (si no indicó ninguna, puede ser privado).
3. Créalo **vacío**, sin README, licencia ni `.gitignore`; esos archivos ya están en el proyecto.
4. Copia la URL HTTPS que muestra GitHub. Abre una terminal y clona el repositorio vacío en una carpeta nueva:

```bash
git clone TU_URL_DE_GITHUB "$HOME/Documents/directorio-funcionarios"
```

Reemplaza `TU_URL_DE_GITHUB` por la URL real. Copia el contenido del proyecto (incluidos los archivos ocultos `.gitignore` y `.gitattributes`) a esa carpeta clonada. En macOS, con la ubicación de este computador:

```bash
cp -R "$HOME/Desktop/TODO DE PROGRAMACION/SPRING BOOT/actividadenclase/." \
  "$HOME/Documents/directorio-funcionarios/"
```

Si el proyecto está en otra ubicación, reemplaza la ruta de origen. Luego registra y publica los archivos:

```bash
cd "$HOME/Documents/directorio-funcionarios"
git status
git add .
git commit -m "Crear directorio de funcionarios"
git branch -M main
git push -u origin main
```

Si Git pide identificarte por primera vez, configura el nombre y correo que quieres que aparezcan en tus commits:

```bash
git config --global user.name "Tu nombre"
git config --global user.email "tu-correo@example.com"
```

Después de cambiar código, repite `git add .`, `git commit -m "Describe tu cambio"` y `git push`. No publiques contraseñas personales ni las uses en lugar de la contraseña de ejemplo del taller.

## 8. Endpoints REST

La ruta base es `/api/v1/funcionarios`. Los campos obligatorios son `nombre`, `cargo` y `departamento`; `telefono` es opcional. Los errores JSON usan la forma `{"fecha":"...","estado":404,"mensaje":"..."}`. Toda respuesta `204` se envía sin cuerpo.

| Método | Ruta | Qué hace | Respuestas principales |
|---|---|---|---|
| `GET` | `/api/v1/funcionarios` | Lista todos los funcionarios. | `200` con la lista, `204` si está vacío |
| `GET` | `/api/v1/funcionarios/{id}` | Consulta un funcionario por ID. | `200`, `404` |
| `GET` | `/api/v1/funcionarios?nombre=ana` | Busca nombres que contengan el fragmento, sin distinguir mayúsculas. | `200` con coincidencias, `204` sin resultados |
| `GET` | `/api/v1/funcionarios?departamento=Contabilidad` | Busca el nombre completo del departamento, sin distinguir mayúsculas. | `200` con coincidencias, `204` sin resultados |
| `POST` | `/api/v1/funcionarios` | Crea un registro; el ID se omite o se envía como `null`. El servidor genera el ID. | `201`, `400` |
| `PUT` | `/api/v1/funcionarios/{id}` | Actualiza los campos; el ID de la ruta identifica el registro. | `200`, `400`, `404` |
| `DELETE` | `/api/v1/funcionarios/{id}` | Elimina el registro indicado. | `204`, `404` |

Para `GET /api/v1/funcionarios`, ambos filtros pueden omitirse, estar vacíos o contener solo espacios; en ese caso se listan todos. Si llegan los dos con valor, se usa `nombre`. El nombre se busca parcialmente; el departamento debe coincidir completo. Ambas búsquedas ignoran mayúsculas. Una lista vacía se representa con `204`, no con `200 []`. La API responde `400` si el JSON está mal formado, faltan campos obligatorios o el ID al crear no es nulo.

## 9. Ejemplos con curl

En Windows puedes usar `curl.exe` en PowerShell. En macOS/Linux usa `curl`.

```bash
# Listar todo (200 si hay datos; 204 si está vacío)
curl -i http://localhost:8080/api/v1/funcionarios

# Buscar el ID 1 (si no existe, la respuesta es 404)
curl -i http://localhost:8080/api/v1/funcionarios/1

# Buscar una parte del nombre
curl -i "http://localhost:8080/api/v1/funcionarios?nombre=Ana"

# Buscar un departamento
curl -i "http://localhost:8080/api/v1/funcionarios?departamento=Contabilidad"

# Si llegan ambos filtros, se usa el nombre
curl -i "http://localhost:8080/api/v1/funcionarios?nombre=Ana&departamento=Talento%20Humano"

# Crear (la respuesta será 201 e incluirá el ID generado)
curl -i -X POST http://localhost:8080/api/v1/funcionarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Sofía Torres","cargo":"Auxiliar","departamento":"Contabilidad","telefono":"3005550101"}'

# También es válido omitir telefono e ID, o enviar "id": null

# Probar validación (400: falta departamento)
curl -i -X POST http://localhost:8080/api/v1/funcionarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Sofía Torres","cargo":"Auxiliar"}'

# Probar validación (400: al crear no se permite un ID no nulo)
curl -i -X POST http://localhost:8080/api/v1/funcionarios \
  -H "Content-Type: application/json" \
  -d '{"id":7,"nombre":"Sofía Torres","cargo":"Auxiliar","departamento":"Contabilidad"}'

# Actualizar el ID 1; el ID no se envía en el JSON
curl -i -X PUT http://localhost:8080/api/v1/funcionarios/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Gómez","cargo":"Contadora sénior","departamento":"Contabilidad","telefono":"3001234567"}'

# Eliminar el ID 1 (204 si existe)
curl -i -X DELETE http://localhost:8080/api/v1/funcionarios/1
```

## 10. Lista de verificación del taller

- [ ] Al abrir la página aparecen en la tabla los registros guardados.
- [ ] Crear un funcionario con nombre, cargo, departamento y sin teléfono funciona.
- [ ] Si se omite nombre, cargo o departamento, el formulario lo impide y la API devuelve `400`.
- [ ] En la tabla, pulsar **Editar** carga el mismo formulario con sus datos.
- [ ] Guardar cambios modifica los datos, pero mantiene el mismo ID.
- [ ] **Limpiar formulario** permite empezar un registro nuevo.
- [ ] Buscar por ID existente muestra solo ese registro; por ID inexistente informa que no existe y deja la tabla sin filas de datos.
- [ ] Buscar una parte del nombre, por ejemplo `Ana`, devuelve coincidencias; un texto sin coincidencias muestra un mensaje y tabla vacía (la API responde `204`).
- [ ] Buscar `Contabilidad` muestra a Ana y Luis del ejemplo.
- [ ] **Volver al listado completo** vuelve a cargar todos los registros.
- [ ] **Eliminar** pide confirmación; al cancelar, no envía la petición y conserva el registro; al aceptar, lo elimina.
- [ ] `GET /api/v1/funcionarios/999999` responde `404` si ese ID no existe.
- [ ] Un directorio vacío, o una búsqueda sin coincidencias, responde `204` sin cuerpo.
- [ ] Crear con un ID numérico no nulo responde `400`; sin ID o con `"id": null` crea el registro.

## 11. Errores comunes

| Mensaje o síntoma | Causa probable | Solución |
|---|---|---|
| `Access denied for user` / `Public Key Retrieval is not allowed` | Usuario o clave incorrectos; el usuario no tiene permisos; no coincide con la clave configurada. | Revisa `DB_USER` y `DB_PASSWORD`, ejecuta el `GRANT` del script como administrador y comprueba el host `'localhost'`. La URL incluida habilita la recuperación de clave para MySQL local. |
| `Communications link failure` / `Connection refused` | MySQL está detenido, escucha en otro puerto o el nombre del servicio es distinto. | Inicia el servicio, comprueba el puerto `3306` y revisa `spring.datasource.url`. |
| `Table '...funcionarios' doesn't exist` o error de validación de esquema | No se ejecutó el script, se creó la base con otro nombre o la tabla no coincide. | Ejecuta `src/main/resources/db/funcionarios.sql` y confirma que la URL apunta a `directorio_funcionarios`. |
| `Port 8080 was already in use` | Otra aplicación usa el puerto. | Cambia `server.port` o `SERVER_PORT` a `8081` y navega a `http://localhost:8081/`. |
| `release version 21 not supported` o Java muestra 17 | Maven o el IDE está usando un JDK anterior. | Configura `JAVA_HOME`, `PATH` y el SDK del IDE a JDK 21; abre una terminal nueva y verifica `java -version` y `./mvnw -version`/`mvnw.cmd -version`. |
| `mysql: command not found` / no se reconoce el comando | La carpeta de ejecutables de MySQL no está en `PATH`. | Usa la terminal de MySQL Workbench/instalador o agrega la carpeta `bin` de MySQL al `PATH`. |
| El IDE muestra errores de dependencias | Maven no terminó de importar o no tiene conexión para descargarlas. | Confirma internet, pulsa **Reload/Refresh Maven Project** y espera la importación. No necesitas instalar Lombok ni habilitar annotation processing. |

## 12. Cómo viaja una petición

El navegador crea una petición `fetch` al endpoint REST. El controlador recibe la ruta, valida el cuerpo cuando es una creación o edición y delega la operación al servicio. El servicio aplica las reglas, y el repositorio de Spring Data JPA traduce la consulta a SQL para MySQL. La respuesta vuelve como JSON con el código HTTP correspondiente; JavaScript actualiza la tabla o muestra el mensaje de error.
