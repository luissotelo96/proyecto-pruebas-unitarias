package org.example;

/**
 * Métricas de evaluación del reconocimiento de señas LSC.
 * En el sistema de traducción, estas métricas permiten medir
 * qué tan bien el modelo identifica las señas capturadas por la cámara.
 */
public final class MetricasReconocimiento {

    /**
     * Resultado de una evaluación de precisión.
     *
     * @param porcentaje   precisión de 0.0 a 100.0, con dos decimales
     * @param nivel        clasificación: insuficiente, aceptable, buena o excelente
     * @param cumpleUmbral true si el porcentaje alcanza el umbral mínimo exigido
     */
    public record ResultadoPrecision(double porcentaje, String nivel, boolean cumpleUmbral) {
    }

    private MetricasReconocimiento() {
    }

    /**
     * Calcula y clasifica la precisión del reconocimiento de señas.
     *
     * <p><b>Para qué sirve:</b> evaluar si el modelo de IA que traduce
     * Lengua de Señas Colombiana (LSC) a texto o voz está listo para usarse.
     * El proyecto mide la precisión del reconocimiento en pruebas funcionales
     * y toma como referencia rangos altos (alrededor del 88% al 98%).</p>
     *
     * <p><b>Qué hace:</b></p>
     * <ol>
     *   <li>Valida aciertos, total y umbral.</li>
     *   <li>Calcula el porcentaje (aciertos / total × 100) y lo redondea a 2 decimales.</li>
     *   <li>Clasifica el nivel: insuficiente (&lt; 70), aceptable (70 a &lt; 88),
     *       buena (88 a &lt; 95) o excelente (≥ 95).</li>
     *   <li>Indica si ese porcentaje cumple el umbral mínimo que exige el sistema.</li>
     * </ol>
     *
     * <p><b>Qué debe recibir:</b>
     * <ul>
     *   <li>{@code aciertos} — señas reconocidas correctamente (≥ 0 y ≤ total)</li>
     *   <li>{@code total} — señas evaluadas (&gt; 0)</li>
     *   <li>{@code umbralMinimo} — porcentaje mínimo aceptable para el sistema (0 a 100)</li>
     * </ul>
     * </p>
     *
     * <p><b>Qué devuelve:</b> un {@link ResultadoPrecision} con el porcentaje,
     * el nivel y si cumple el umbral.
     * Ejemplo: 8 aciertos de 10, umbral 75 → 80.0, "aceptable", true.</p>
     *
     * @param aciertos      señas clasificadas correctamente
     * @param total         total de señas evaluadas
     * @param umbralMinimo  porcentaje mínimo que el sistema considera aceptable
     * @return porcentaje, nivel y si cumple el umbral
     * @throws IllegalArgumentException si los datos de entrada no son válidos
     */
    public static ResultadoPrecision calcularPrecision(int aciertos, int total, double umbralMinimo) {
        if (aciertos < 0) {
            throw new IllegalArgumentException("Los aciertos no pueden ser negativos");
        }
        if (total <= 0) {
            throw new IllegalArgumentException("El total debe ser mayor que cero");
        }
        if (aciertos > total) {
            throw new IllegalArgumentException("Los aciertos no pueden superar el total");
        }
        if (umbralMinimo < 0 || umbralMinimo > 100) {
            throw new IllegalArgumentException("El umbral debe estar entre 0 y 100");
        }

        double bruto = (aciertos * 100.0) / total;
        double porcentaje = Math.round(bruto * 100.0) / 100.0;

        return new ResultadoPrecision(porcentaje, clasificarNivel(porcentaje), porcentaje >= umbralMinimo);
    }

    private static String clasificarNivel(double porcentaje) {
        if (porcentaje >= 95) {
            return "excelente";
        }
        if (porcentaje >= 88) {
            return "buena";
        }
        if (porcentaje >= 70) {
            return "aceptable";
        }
        return "insuficiente";
    }
}
