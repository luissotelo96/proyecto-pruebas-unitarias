package org.example;

import org.example.MetricasReconocimiento.ResultadoPrecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricasReconocimientoTest {

    @Test
    void calcularPrecision_clasificaComoAceptable_cuandoSuperaElUmbral() {
        // Arrange
        int aciertos = 8;
        int total = 10;
        double umbralMinimo = 75;

        // Act
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(aciertos, total, umbralMinimo);

        // Assert: 8/10 = 80.00 → aceptable y cumple 75
        assertEquals(80.0, resultado.porcentaje(), 0.001);
        assertEquals("aceptable", resultado.nivel());
        assertTrue(resultado.cumpleUmbral());
    }

    @Test
    void calcularPrecision_clasificaComoExcelente_cuandoElPorcentajeEs100() {
        // Arrange
        int aciertos = 5;
        int total = 5;
        double umbralMinimo = 90;

        // Act
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(aciertos, total, umbralMinimo);

        // Assert
        assertEquals(100.0, resultado.porcentaje(), 0.001);
        assertEquals("excelente", resultado.nivel());
        assertTrue(resultado.cumpleUmbral());
    }

    @Test
    void calcularPrecision_clasificaComoBuena_aunqueNoCumplaUnUmbralMasEstricto() {
        // Arrange: 88% es "buena", pero el sistema exige 90
        int aciertos = 22;
        int total = 25;
        double umbralMinimo = 90;

        // Act
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(aciertos, total, umbralMinimo);

        // Assert
        assertEquals(88.0, resultado.porcentaje(), 0.001);
        assertEquals("buena", resultado.nivel());
        assertFalse(resultado.cumpleUmbral());
    }

    @Test
    void calcularPrecision_redondeaADosDecimales_yQuedaInsuficiente() {
        // Arrange: 2/3 = 66.666... → 66.67
        int aciertos = 2;
        int total = 3;
        double umbralMinimo = 70;

        // Act
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(aciertos, total, umbralMinimo);

        // Assert
        assertEquals(66.67, resultado.porcentaje(), 0.001);
        assertEquals("insuficiente", resultado.nivel());
        assertFalse(resultado.cumpleUmbral());
    }

    @Test
    void calcularPrecision_devuelveCero_cuandoNoHayAciertos() {
        // Arrange
        int aciertos = 0;
        int total = 20;
        double umbralMinimo = 70;

        // Act
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(aciertos, total, umbralMinimo);

        // Assert
        assertEquals(0.0, resultado.porcentaje(), 0.001);
        assertEquals("insuficiente", resultado.nivel());
        assertFalse(resultado.cumpleUmbral());
    }

    @Test
    void calcularPrecision_lanzaExcepcion_cuandoTotalEsCero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MetricasReconocimiento.calcularPrecision(0, 0, 70)
        );
    }

    @Test
    void calcularPrecision_lanzaExcepcion_cuandoAciertosSonNegativos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MetricasReconocimiento.calcularPrecision(-1, 10, 70)
        );
    }

    @Test
    void calcularPrecision_lanzaExcepcion_cuandoAciertosSuperanElTotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MetricasReconocimiento.calcularPrecision(12, 10, 70)
        );
    }

    @Test
    void calcularPrecision_lanzaExcepcion_cuandoElUmbralEstaFueraDeRango() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MetricasReconocimiento.calcularPrecision(8, 10, 120)
        );
    }
}
