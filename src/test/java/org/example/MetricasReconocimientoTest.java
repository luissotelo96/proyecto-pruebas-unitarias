package org.example;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.MetricasReconocimiento.ResultadoPrecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Casos de prueba de la etapa 2 para el módulo de métricas de reconocimiento.
 * Cada prueba lleva el código del caso (CPxx) en el nombre del método o en el
 * comentario de su fila, y el requisito funcional (RF-xx) que verifica.
 *
 * <p>Pasos del plan: (1) ejecutar calcularPrecision() con los datos del caso,
 * (2) consultar los atributos retornados o la excepción, (3) comparar con el
 * resultado esperado. El registro del resultado lo hace el reporte de JUnit.</p>
 */
@DisplayName("Módulo de métricas de reconocimiento LSC")
class MetricasReconocimientoTest {

    @Test
    @DisplayName("CP01 - Clasificación aceptable (8, 10, 75) → 80 %, aceptable, true [RF-01, RF-03, RF-04]")
    void CP01_clasificacionAceptable() {
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(8, 10, 75);

        assertAll(
                () -> assertEquals(80.0, resultado.porcentaje(), 0.001),
                () -> assertEquals("aceptable", resultado.nivel()),
                () -> assertTrue(resultado.cumpleUmbral())
        );
    }

    @Test
    @DisplayName("CP02 - Clasificación excelente (5, 5, 90) → 100 %, excelente, true [RF-01, RF-03, RF-04]")
    void CP02_clasificacionExcelente() {
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(5, 5, 90);

        assertAll(
                () -> assertEquals(100.0, resultado.porcentaje(), 0.001),
                () -> assertEquals("excelente", resultado.nivel()),
                () -> assertTrue(resultado.cumpleUmbral())
        );
    }

    @Test
    @DisplayName("CP03 - Clasificación buena sin cumplir el umbral (22, 25, 90) → 88 %, buena, false [RF-03, RF-04]")
    void CP03_clasificacionBuenaSinCumplirUmbral() {
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(22, 25, 90);

        assertAll(
                () -> assertEquals(88.0, resultado.porcentaje(), 0.001),
                () -> assertEquals("buena", resultado.nivel()),
                () -> assertFalse(resultado.cumpleUmbral())
        );
    }

    @Test
    @DisplayName("CP04 - Redondeo a dos decimales (2, 3, 70) → 66,67 % [RF-01]")
    void CP04_redondeoADosDecimales() {
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(2, 3, 70);

        assertEquals(66.67, resultado.porcentaje(), 0.0001);
    }

    @Test
    @DisplayName("CP05 - Cero aciertos (0, 20, 70) → 0 %, insuficiente, false [RF-05]")
    void CP05_ceroAciertos() {
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(0, 20, 70);

        assertAll(
                () -> assertEquals(0.0, resultado.porcentaje(), 0.001),
                () -> assertEquals("insuficiente", resultado.nivel()),
                () -> assertFalse(resultado.cumpleUmbral())
        );
    }

    /**
     * CP06, CP07, CP08: el mismo caso (rechazar aciertos/total inválidos) con
     * distintos valores, incluido el límite total = 0 y aciertos = -1.
     */
    @ParameterizedTest(name = "{0} - {1} ({2}, {3}, {4}) → IllegalArgumentException")
    @DisplayName("CP06, CP07, CP08 - Aciertos o total inválidos son rechazados [RF-02]")
    @CsvSource({
            "CP06, Total igual a cero,           0,  0,  70, El total debe ser mayor que cero",
            "CP07, Aciertos negativos,           -1, 10, 70, Los aciertos no pueden ser negativos",
            "CP08, Aciertos superiores al total, 12, 10, 70, Los aciertos no pueden superar el total"
    })
    void CP06_CP07_CP08_aciertosOTotalInvalidosSonRechazados(String codigo, String caso,
            int aciertos, int total, double umbral, String mensajeEsperado) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> MetricasReconocimiento.calcularPrecision(aciertos, total, umbral));

        assertEquals(mensajeEsperado, error.getMessage());
    }

    /**
     * CP09, CP10, CP13: valores límite inválidos del umbral (fuera de 0..100).
     * CP13 (101) es el valor inmediatamente posterior al límite superior.
     */
    @ParameterizedTest(name = "{0} - {1} (8, 10, {2}) → IllegalArgumentException")
    @DisplayName("CP09, CP10, CP13 - Umbral fuera del rango 0..100 es rechazado [RF-02]")
    @CsvSource({
            "CP09, Umbral superior al permitido,                  120",
            "CP10, Umbral negativo,                               -1",
            "CP13, Valor posterior al límite superior del umbral, 101"
    })
    void CP09_CP10_CP13_umbralFueraDeRangoEsRechazado(String codigo, String caso, double umbral) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> MetricasReconocimiento.calcularPrecision(8, 10, umbral));

        assertEquals("El umbral debe estar entre 0 y 100", error.getMessage());
    }

    /**
     * CP11, CP12, CP14, CP15: valores límite válidos del umbral y de la clasificación.
     */
    @ParameterizedTest(name = "{0} - {1} ({2}, {3}, {4}) → {5} %, {6}, {7}")
    @DisplayName("CP11, CP12, CP14, CP15 - Valores límite válidos de umbral y clasificación [RF-02, RF-03, RF-04]")
    @CsvSource({
            "CP11, Límite inferior del umbral,                   8,  10,  0,   80.0,  aceptable, true",
            "CP12, Límite superior del umbral,                   8,  10,  100, 80.0,  aceptable, false",
            "CP14, Porcentaje igual al umbral,                   7,  10,  70,  70.0,  aceptable, true",
            "CP15, Límite inferior de la clasificación excelente, 95, 100, 90,  95.0,  excelente, true"
    })
    void CP11_CP12_CP14_CP15_valoresLimiteValidos(String codigo, String caso, int aciertos, int total,
            double umbral, double porcentajeEsperado, String nivelEsperado, boolean cumpleEsperado) {
        ResultadoPrecision resultado = MetricasReconocimiento.calcularPrecision(aciertos, total, umbral);

        assertAll(
                () -> assertEquals(porcentajeEsperado, resultado.porcentaje(), 0.001),
                () -> assertEquals(nivelEsperado, resultado.nivel()),
                () -> assertEquals(cumpleEsperado, resultado.cumpleUmbral())
        );
    }
}
