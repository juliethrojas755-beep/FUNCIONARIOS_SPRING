package com.campuslands.c3.actividadenclase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada: inicia Spring Boot y configura los componentes del proyecto.
 */
@SpringBootApplication
public class ActividadenclaseApplication {

	public static void main(String[] args) {
		// Inicia el servidor web que publica la página y la API REST.
		SpringApplication.run(ActividadenclaseApplication.class, args);
	}

}
