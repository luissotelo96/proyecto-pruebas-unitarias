# Etapa 3 – Automatización de los casos de prueba (JUnit 5)

Proyecto: sistema de traducción de Lengua de Señas Colombiana (LSC).
Base: plan de pruebas y casos CP01–CP15 de la etapa 2.

## Cómo ejecutar

```bash
mvn test
```

Reporte HTML (queda en `target/site/surefire-report.html`):

```bash
mvn test org.apache.maven.plugins:maven-surefire-report-plugin:3.2.5:report-only
```

La evidencia de esta ejecución está en [`docs/reporte-junit/surefire-report.html`](reporte-junit/surefire-report.html), junto con los `.txt` y `.xml` de Surefire. El `pom.xml` está configurado para que el reporte muestre el `@DisplayName` de cada prueba, así cada fila aparece con su código CP.

## Clases de prueba

| Clase | Nivel | Casos | Clases reales que intervienen |
|---|---|---|---|
| [`MetricasReconocimientoTest`](../src/test/java/org/example/MetricasReconocimientoTest.java) | Unitaria | CP01–CP15 | `MetricasReconocimiento` |
| [`AlmacenamientoConsultaIntegracionTest`](../src/test/java/org/example/lsc/AlmacenamientoConsultaIntegracionTest.java) | Integración | CP16–CP18 | `RepositorioTraducciones`, `SenaRepository`, `TraduccionRepository`, `Sena`, `Traduccion`, `MetricasReconocimiento` (H2 en memoria) |

Se eliminaron `EjemploMetodos` y `UtilidadesTest` porque no pertenecen al objetivo del proyecto. Las 9 pruebas que ya tenía `MetricasReconocimientoTest` eran los mismos escenarios de CP01–CP09, pero sin código; se reescribieron según el plan para no duplicarlas.

## Casos automatizados

Los 15 casos de la etapa 2 quedaron automatizados con los mismos datos de entrada y resultados esperados del plan. Cuando un mismo caso se repite con distintos valores (valores límite y entradas inválidas) se usa `@ParameterizedTest`, y cada fila lleva su código CP.

| Código | Caso | Requisito | Técnica | Método JUnit |
|---|---|---|---|---|
| CP01 | Clasificación aceptable | RF-01, RF-03, RF-04 | Partición de equivalencia | `CP01_clasificacionAceptable` |
| CP02 | Clasificación excelente | RF-01, RF-03, RF-04 | Partición de equivalencia | `CP02_clasificacionExcelente` |
| CP03 | Clasificación buena sin cumplir el umbral | RF-03, RF-04 | Tabla de decisión | `CP03_clasificacionBuenaSinCumplirUmbral` |
| CP04 | Redondeo a dos decimales | RF-01 | Partición de equivalencia | `CP04_redondeoADosDecimales` |
| CP05 | Cero aciertos | RF-05 | Valor límite | `CP05_ceroAciertos` |
| CP06 | Total igual a cero | RF-02 | Valor límite (inválido) | `CP06_CP07_CP08_aciertosOTotalInvalidosSonRechazados` (`@ParameterizedTest`) |
| CP07 | Aciertos negativos | RF-02 | Valor límite (inválido) | ídem |
| CP08 | Aciertos superiores al total | RF-02 | Partición inválida | ídem |
| CP09 | Umbral superior al permitido | RF-02 | Partición inválida | `CP09_CP10_CP13_umbralFueraDeRangoEsRechazado` (`@ParameterizedTest`) |
| CP10 | Umbral negativo | RF-02 | Valor límite (inválido) | ídem |
| CP13 | Valor posterior al límite superior del umbral | RF-02 | Valor límite (inválido) | ídem |
| CP11 | Límite inferior del umbral | RF-02, RF-04 | Valor límite | `CP11_CP12_CP14_CP15_valoresLimiteValidos` (`@ParameterizedTest`) |
| CP12 | Límite superior del umbral | RF-02, RF-04 | Valor límite | ídem |
| CP14 | Porcentaje igual al umbral | RF-03, RF-04 | Valor límite | ídem |
| CP15 | Límite inferior de la clasificación excelente | RF-03 | Valor límite | ídem |
| CP16 | Las traducciones guardadas se consultan en el historial, de la más reciente a la más antigua | *Propuesto: RF-06, RF-07* | **Integración** | `CP16_traduccionesAlmacenadasSeConsultanEnElHistorial` |
| CP17 | Una traducción inválida se rechaza y no queda almacenada | *Propuesto: RF-06* | **Integración** | `CP17_traduccionInvalidaNoSeAlmacena` |
| CP18 | La precisión se calcula sobre el historial almacenado | RF-01, RF-03, RF-04 | **Integración** | `CP18_precisionCalculadaSobreElHistorialAlmacenado` |

### Casos de integración agregados (CP16–CP18)

El plan define el nivel de integración como *"verificación de la comunicación entre las clases responsables del almacenamiento y la consulta de traducciones"*, pero la etapa 2 no tenía casos para ese nivel. Por eso se agregaron estos tres, que deben incorporarse al documento de casos y a la matriz:

| ID | Precondición | Datos de entrada | Resultado esperado |
|---|---|---|---|
| CP16 | Señas HOLA y AGUA registradas | Traducciones (HOLA, texto, 0.94), (AGUA, voz, 0.88), (HOLA, voz, 0.60); consultar historial con límite 2 | 3 almacenadas; el historial devuelve 2, primero HOLA/voz y luego AGUA; 2 en modo voz; promedio de confianza 0.81 |
| CP17 | Seña HOLA registrada | Confianza 1.5; seña inexistente CASA; modo "audio" | Cada intento lanza IllegalArgumentException y no se almacena ninguna traducción |
| CP18 | Seña MAMA registrada | Confianzas 0.95, 0.91, 0.88, 0.60, 0.92; acierto si confianza ≥ 0.88; umbral 75 | 4 de 5 → 80 %, aceptable, true |

**Observación para la matriz de trazabilidad:** el alcance incluye *"Validación y almacenamiento de traducciones"* y *"Consulta del historial de traducciones recientes"*, pero ninguno tiene un RF en la tabla de requisitos. Se sugiere agregar:

| ID | Requisito funcional | Casos asociados | Cobertura |
|---|---|---|---|
| RF-06 | Validación y almacenamiento de traducciones | CP16, CP17 | Cubierto |
| RF-07 | Consulta del historial de traducciones recientes | CP16 | Cubierto |

Además, CP18 suma cobertura a RF-01, RF-03 y RF-04.

## Registro de resultados obtenidos

Ejecución del 18/11/2026 con `mvn test` (JDK 22, JUnit 5, Maven).

| ID | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|
| CP01 | 80 %, aceptable, true | 80.0 %, aceptable, true | Aprobado |
| CP02 | 100 %, excelente, true | 100.0 %, excelente, true | Aprobado |
| CP03 | 88 %, buena, false | 88.0 %, buena, false | Aprobado |
| CP04 | 66,67 % | 66.67 % | Aprobado |
| CP05 | 0 %, insuficiente, false | 0.0 %, insuficiente, false | Aprobado |
| CP06 | IllegalArgumentException | IllegalArgumentException: "El total debe ser mayor que cero" | Aprobado |
| CP07 | IllegalArgumentException | IllegalArgumentException: "Los aciertos no pueden ser negativos" | Aprobado |
| CP08 | IllegalArgumentException | IllegalArgumentException: "Los aciertos no pueden superar el total" | Aprobado |
| CP09 | IllegalArgumentException | IllegalArgumentException: "El umbral debe estar entre 0 y 100" | Aprobado |
| CP10 | IllegalArgumentException | IllegalArgumentException: "El umbral debe estar entre 0 y 100" | Aprobado |
| CP11 | 80 %, aceptable, true | 80.0 %, aceptable, true | Aprobado |
| CP12 | 80 %, aceptable, false | 80.0 %, aceptable, false | Aprobado |
| CP13 | IllegalArgumentException | IllegalArgumentException: "El umbral debe estar entre 0 y 100" | Aprobado |
| CP14 | 70 %, aceptable, true | 70.0 %, aceptable, true | Aprobado |
| CP15 | 95 %, excelente, true | 95.0 %, excelente, true | Aprobado |
| CP16 | Historial ordenado, 2 en voz, promedio 0.81 | Según lo esperado | Aprobado |
| CP17 | Excepción y 0 traducciones almacenadas | Según lo esperado | Aprobado |
| CP18 | 80 %, aceptable, true | 80.0 %, aceptable, true | Aprobado |

No se encontraron defectos.

## Reporte de ejecución JUnit

```
Test set: org.example.MetricasReconocimientoTest
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s

Test set: org.example.lsc.AlmacenamientoConsultaIntegracionTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.779 s

Test set: org.example.lsc.RepositorioTraduccionesTest
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.067 s

Test set: org.example.lsc.ConexionBaseDatosTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.540 s

TOTAL: 32 tests, 0 failures, 0 errors, 0 skipped — 100 % de éxito
```

`RepositorioTraduccionesTest` y `ConexionBaseDatosTest` ya existían en el proyecto. No corresponden a casos de la etapa 2, pero se conservan como pruebas de regresión del módulo de persistencia.

## Cumplimiento de los criterios de salida

| Criterio | Estado |
|---|---|
| Ejecutar como mínimo los 15 casos definidos | Cumplido: 15 de la etapa 2 + 3 de integración |
| 100 % de los requisitos del alcance con al menos un caso | Cumplido para RF-01 a RF-05; RF-06 y RF-07 propuestos y cubiertos |
| Al menos 10 casos automatizados con JUnit 5 | Cumplido: 18 automatizados |
| Sin defectos críticos o de severidad alta pendientes | Cumplido: 0 defectos |
| Resultados documentados | Cumplido: este documento |
| Evidencias y reportes de ejecución | Cumplido: `docs/reporte-junit/` |

## Casos que quedan manuales y por qué

Los 15 casos de la etapa 2 se automatizaron. Todos tienen entradas exactas y un resultado esperado que se puede comparar con una aserción, así que hacerlos a mano no aporta nada y sería más lento y propenso a errores.

Quedan como pruebas manuales las funcionalidades que el plan dejó fuera del alcance. Cuando se integren, se probarán así:

| Funcionalidad excluida | Por qué no se automatiza con JUnit |
|---|---|
| Reconocimiento de señas con la cámara en tiempo real | Requiere una persona señando, una cámara física y el modelo de IA, que aún no está integrado. El resultado varía con la luz, el encuadre y quien hace la seña, así que no hay un valor esperado fijo para una aserción. Lo que sí se automatizó es el cálculo de precisión sobre esos resultados (CP01–CP15, CP18). |
| Rendimiento del modelo de inteligencia artificial | Se evalúa con un conjunto de datos etiquetado y métricas estadísticas, no con una prueba unitaria de resultado exacto. |
| Conversión de texto a voz | Si la voz es clara y natural lo juzga una persona escuchando; no hay una aserción objetiva. |
| Usabilidad y accesibilidad de la aplicación móvil | Necesita la interacción de usuarios reales, incluidas personas sordas, y criterios de percepción. |
| Pruebas de carga y estrés | Se hacen con herramientas como JMeter o Gatling en un entorno parecido al de producción; JUnit no mide eso de forma confiable. |
