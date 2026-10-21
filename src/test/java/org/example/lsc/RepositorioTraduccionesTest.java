package org.example.lsc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.example.lsc.entity.Sena;
import org.example.lsc.entity.Traduccion;
import org.example.lsc.service.RepositorioTraducciones;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(RepositorioTraducciones.class)
class RepositorioTraduccionesTest {

    @Autowired
    private RepositorioTraducciones repositorio;

    @Test
    void registrarSena_guardaLaSenaEnLaBase() {
        Sena sena = repositorio.registrarSena("hola", "Hola", "Saludo");

        assertEquals("HOLA", sena.getCodigo());
        assertEquals("hola", sena.getPalabra());
        assertEquals("saludo", sena.getCategoria());
        assertTrue(repositorio.buscarPalabra("HOLA").isPresent());
    }

    @Test
    void registrarSena_lanzaExcepcion_cuandoElCodigoYaExiste() {
        repositorio.registrarSena("HOLA", "hola", "saludo");

        assertThrows(IllegalArgumentException.class, () -> repositorio.registrarSena("HOLA", "hola", "saludo"));
    }

    @Test
    void buscarPalabra_devuelveLaTraduccion_cuandoLaSenaExiste() {
        repositorio.registrarSena("AGUA", "agua", "necesidad");

        assertEquals("agua", repositorio.buscarPalabra("agua").orElseThrow());
    }

    @Test
    void buscarPalabra_devuelveVacio_cuandoLaSenaNoExiste() {
        assertTrue(repositorio.buscarPalabra("CASA").isEmpty());
    }

    @Test
    void listarSenasPorCategoria_devuelveSoloLasDeEsaCategoria() {
        repositorio.registrarSena("HOLA", "hola", "saludo");
        repositorio.registrarSena("ADIOS", "adiós", "saludo");
        repositorio.registrarSena("MAMA", "mamá", "familia");

        List<Sena> saludos = repositorio.listarSenasPorCategoria("saludo");

        assertEquals(2, saludos.size());
        assertEquals("adiós", saludos.get(0).getPalabra());
        assertEquals("hola", saludos.get(1).getPalabra());
    }

    @Test
    void guardarTraduccion_persisteElHistorial() {
        repositorio.registrarSena("HOLA", "hola", "saludo");

        Traduccion traduccion = repositorio.guardarTraduccion("HOLA", "hola", "texto", 0.92);

        assertEquals("HOLA", traduccion.getCodigoSena());
        assertEquals("texto", traduccion.getModo());
        assertEquals(0.92, traduccion.getConfianza(), 0.001);
        assertEquals(1, repositorio.listarHistorialReciente(5).size());
    }

    @Test
    void guardarTraduccion_lanzaExcepcion_cuandoLaConfianzaEsInvalida() {
        repositorio.registrarSena("HOLA", "hola", "saludo");

        assertThrows(
                IllegalArgumentException.class,
                () -> repositorio.guardarTraduccion("HOLA", "hola", "texto", 1.5)
        );
    }

    @Test
    void listarHistorialReciente_devuelveLasMasNuevasPrimero() {
        repositorio.registrarSena("HOLA", "hola", "saludo");
        repositorio.registrarSena("AGUA", "agua", "necesidad");
        repositorio.guardarTraduccion("HOLA", "hola", "texto", 0.9);
        repositorio.guardarTraduccion("AGUA", "agua", "voz", 0.8);

        List<Traduccion> historial = repositorio.listarHistorialReciente(1);

        assertEquals(1, historial.size());
        assertEquals("AGUA", historial.get(0).getCodigoSena());
    }

    @Test
    void contarTraduccionesPorModo_separaTextoYVoz() {
        repositorio.registrarSena("HOLA", "hola", "saludo");
        repositorio.registrarSena("AGUA", "agua", "necesidad");
        repositorio.guardarTraduccion("HOLA", "hola", "texto", 0.9);
        repositorio.guardarTraduccion("HOLA", "hola", "texto", 0.85);
        repositorio.guardarTraduccion("AGUA", "agua", "voz", 0.8);

        assertEquals(2, repositorio.contarTraduccionesPorModo("texto"));
        assertEquals(1, repositorio.contarTraduccionesPorModo("voz"));
    }

    @Test
    void promedioConfianza_calculaElPromedioRedondeado() {
        repositorio.registrarSena("HOLA", "hola", "saludo");
        repositorio.guardarTraduccion("HOLA", "hola", "texto", 0.90);
        repositorio.guardarTraduccion("HOLA", "hola", "voz", 0.80);

        assertEquals(0.85, repositorio.promedioConfianza(), 0.001);
    }

    @Test
    void promedioConfianza_devuelveCero_cuandoNoHayTraducciones() {
        assertEquals(0.0, repositorio.promedioConfianza(), 0.001);
    }

    @Test
    void eliminarSena_quitaLaSenaDelDiccionario() {
        repositorio.registrarSena("ADIOS", "adiós", "saludo");

        assertTrue(repositorio.eliminarSena("adios"));
        assertTrue(repositorio.buscarPalabra("ADIOS").isEmpty());
    }

    @Test
    void eliminarSena_lanzaExcepcion_cuandoNoExiste() {
        assertThrows(IllegalArgumentException.class, () -> repositorio.eliminarSena("CASA"));
    }
}
