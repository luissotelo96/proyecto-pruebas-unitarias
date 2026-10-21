package org.example.lsc.config;

import org.example.lsc.service.RepositorioTraducciones;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DatosPrueba {

    @Bean
    @Profile({"dev", "docker"})
    CommandLineRunner cargarDiccionario(RepositorioTraducciones repositorio) {
        return args -> {
            if (repositorio.buscarPalabra("HOLA").isPresent()) {
                return;
            }

            repositorio.registrarSena("HOLA", "hola", "saludo");
            repositorio.registrarSena("ADIOS", "adiós", "saludo");
            repositorio.registrarSena("GRACIAS", "gracias", "saludo");
            repositorio.registrarSena("MAMA", "mamá", "familia");
            repositorio.registrarSena("PAPA", "papá", "familia");
            repositorio.registrarSena("AGUA", "agua", "necesidad");

            repositorio.guardarTraduccion("HOLA", "hola", "texto", 0.94);
            repositorio.guardarTraduccion("AGUA", "agua", "voz", 0.88);
            repositorio.guardarTraduccion("MAMA", "mamá", "texto", 0.91);
        };
    }
}
