-- Ejecuta este script una sola vez en MySQL, igual que funcionarios.sql, con un usuario
-- administrador. Crea la tabla de cuentas que usa el login/registro del directorio.

USE directorio_funcionarios;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta DATETIME NULL,
    creado_en DATETIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_username (username)
) ENGINE=InnoDB;

-- El usuario de la aplicación (directorio_app, creado en funcionarios.sql) también
-- necesita permisos sobre esta tabla nueva.
GRANT SELECT, INSERT, UPDATE, DELETE
    ON directorio_funcionarios.usuarios
    TO 'directorio_app'@'localhost';
