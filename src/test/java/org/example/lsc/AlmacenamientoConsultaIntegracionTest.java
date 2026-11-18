package org.example.lsc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.example.MetricasReconocimiento;
import org.example.MetricasReconocimiento.ResultadoPrecision;
import org.example.lsc.entity.Traduccion;
import org.example.lsc.repository.TraduccionRepository;
import org.example.lsc.service.RepositorioTraducciones;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Nivel de integración del plan: comunicación entre las clases responsables del
 * almacenamiento y la consulta de traducciones. No usa mocks; trabajan juntas las
 * clases reales RepositorioTraducciones, SenaRepository, TraduccionRepository,
 * las entidades Sena/Traduccion y MetricasReconocimiento, sobre H2 en memoria.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(RepositorioTraducciones.class)
@DisplayName("Integración: almacenamiento y consulta de traducciones")
class AlmacenamientoConsultaIntegracionTest {

    @Autowired
    private RepositorioTraducciones repositorio;

    @Autowired
    private TraduccionRepository traduccionRepository;

    @Test
    @DisplayName("CP16 - Las traducciones guardadas se consultan en el historial, de la más reciente a la más antigua")
    void CP16_traduccionesAlmacenadasSeConsultanEnElHistorial() {
        repositorio.registrarSena("HOLA", "hola", "saludo");
        repositorio.registrarSena("AGUA", "agua", "necesidad");

        repositorio.guardarTraduccion("hola", "hola", "TEXTO", 0.94);
        repositorio.guardarTraduccion("AGUA", "agua", "voz", 0.88);
        repositorio.guardarTraduccion("HOLA", "hola", "voz", 0.60);

        List<Traduccion> historial = repositorio.listarHistorialReciente(2);

        assertAll(
                () -> assertEquals(3, traduccionRepository.count()),
                () -> assertEquals(2, historial.size()),
                () -> assertEquals("HOLA", historial.get(0).getCodigoSena()),
                () -> assertEquals("voz", historial.get(0).getModo()),
                () -> assertEquals("AGUA", historial.get(1).getCodigoSena()),
                () -> assertEquals(2, repositorio.contarTraduccionesPorModo("voz")),
                () -> assertEquals(0.81, repositorio.promedioConfianza(), 0.001)
        );
    }

    @Test
    @DisplayName("CP17 - Una traducción inválida se rechaza y no queda almacenada")
    void CP17_traduccionInvalidaNoSeAlmacena() {
        repositorio.registrarSena("HOLA", "hola", "saludo");

        assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardarTraduccion("HOLA", "hola", "texto", 1.5));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardarTraduccion("CASA", "casa", "texto", 0.9));
        assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardarTraduccion("HOLA", "hola", "audio", 0.9));

        assertEquals(0, traduccionRepository.count());
        assertTrue(repositorio.listarHistorialReciente(5).isEmpty());
    }

    @Test
    @DisplayName("CP18 - La precisión se calcula con MetricasReconocimiento sobre el historial almacenado")
    void CP18_precisionCalculadaSobreElHistorialAlmacenado() {
        repositorio.registrarSena("MAMA", "mamá", "familia");
        for (double confianza : new double[]{0.95, 0.91, 0.88, 0.60, 0.92}) {
            repositorio.guardarTraduccion("MAMA", "mamá", "texto", confianza);
        }

        // Una traducción cuenta como acierto si su confianza es ≥ 0.88 (inicio del nivel "buena").
        List<Traduccion> historial = repositorio.listarHistorialReciente(10);
        int aciertos = (int) historial.stream().filter(t -> t.getConfianza() >= 0.88).count();
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(aciertos, historial.size(), 75);

        assertAll(
                () -> assertEquals(80.0, resultado.porcentaje(), 0.001),
                () -> assertEquals("aceptable", resultado.nivel()),
                () -> assertTrue(resultado.cumpleUmbral())
        );
    }
}
