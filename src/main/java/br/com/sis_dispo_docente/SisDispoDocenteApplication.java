package br.com.sis_dispo_docente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** ms-curso: o @EnableDiscoveryClient é opcional nas versões atuais; basta ter o eureka-client no pom. */
@SpringBootApplication
public class SisDispoDocenteApplication {
    public static void main(String[] args) {
        SpringApplication.run(SisDispoDocenteApplication.class, args);
    }
}
