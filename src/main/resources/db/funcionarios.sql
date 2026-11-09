-- Ejecuta este script una sola vez en MySQL con un usuario administrador.
CREATE DATABASE IF NOT EXISTS directorio_funcionarios
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'directorio_app'@'localhost'
    IDENTIFIED BY 'CambiaEstaClave123!';

GRANT SELECT, INSERT, UPDATE, DELETE
    ON directorio_funcionarios.*
    TO 'directorio_app'@'localhost';

USE directorio_funcionarios;

CREATE TABLE IF NOT EXISTS funcionarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    departamento VARCHAR(100) NOT NULL,
    telefono VARCHAR(30) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO funcionarios (nombre, cargo, departamento, telefono)
SELECT 'Ana Gómez', 'Analista contable', 'Contabilidad', '3001234567'
WHERE NOT EXISTS (SELECT 1 FROM funcionarios WHERE nombre = 'Ana Gómez');

INSERT INTO funcionarios (nombre, cargo, departamento, telefono)
SELECT 'Luis Pérez', 'Contador', 'Contabilidad', '3107654321'
WHERE NOT EXISTS (SELECT 1 FROM funcionarios WHERE nombre = 'Luis Pérez');

INSERT INTO funcionarios (nombre, cargo, departamento, telefono)
SELECT 'María Rodríguez', 'Coordinadora de talento humano', 'Talento Humano', NULL
WHERE NOT EXISTS (SELECT 1 FROM funcionarios WHERE nombre = 'María Rodríguez');
