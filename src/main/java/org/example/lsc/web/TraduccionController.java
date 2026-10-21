package org.example.lsc.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.example.lsc.entity.Sena;
import org.example.lsc.entity.Traduccion;
import org.example.lsc.service.RepositorioTraducciones;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Traducciones LSC", description = "Diccionario de señas e historial de traducciones")
public class TraduccionController {

    private final RepositorioTraducciones repositorio;

    public TraduccionController(RepositorioTraducciones repositorio) {
        this.repositorio = repositorio;
    }

    @PostMapping("/senas")
    @Operation(summary = "Registrar seña", description = "Ejemplo: {\"codigo\":\"GRACIAS\",\"palabra\":\"gracias\",\"categoria\":\"saludo\"}")
    public ResponseEntity<Sena> registrarSena(@RequestBody Map<String, String> body) {
        Sena sena = repositorio.registrarSena(body.get("codigo"), body.get("palabra"), body.get("categoria"));
        return ResponseEntity.status(HttpStatus.CREATED).body(sena);
    }

    @GetMapping("/senas/{codigo}")
    @Operation(summary = "Buscar palabra de una seña", description = "Prueba con HOLA, AGUA o MAMA")
    public ResponseEntity<Map<String, String>> buscarPalabra(@PathVariable String codigo) {
        return repositorio.buscarPalabra(codigo)
                .map(palabra -> ResponseEntity.ok(Map.of("codigo", codigo.toUpperCase(), "palabra", palabra)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No existe la seña " + codigo.toUpperCase())));
    }

    @GetMapping("/senas")
    @Operation(summary = "Listar señas por categoría", description = "Prueba categoria=saludo o categoria=familia")
    public List<Sena> listarPorCategoria(@RequestParam String categoria) {
        return repositorio.listarSenasPorCategoria(categoria);
    }

    @DeleteMapping("/senas/{codigo}")
    @Operation(summary = "Eliminar seña", description = "Prueba con ADIOS (viene en los datos de ejemplo)")
    public Map<String, Boolean> eliminarSena(@PathVariable String codigo) {
        return Map.of("eliminada", repositorio.eliminarSena(codigo));
    }

    @PostMapping("/traducciones")
    @Operation(summary = "Guardar traducción", description = "Ejemplo: {\"codigo\":\"HOLA\",\"texto\":\"hola\",\"modo\":\"texto\",\"confianza\":0.92}")
    public ResponseEntity<Traduccion> guardarTraduccion(@RequestBody Map<String, Object> body) {
        Traduccion traduccion = repositorio.guardarTraduccion(
                String.valueOf(body.get("codigo")),
                String.valueOf(body.get("texto")),
                String.valueOf(body.get("modo")),
                Double.parseDouble(String.valueOf(body.get("confianza")))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(traduccion);
    }

    @GetMapping("/traducciones/recientes")
    @Operation(summary = "Historial reciente", description = "Prueba limite=3")
    public List<Traduccion> historialReciente(@RequestParam(defaultValue = "5") int limite) {
        return repositorio.listarHistorialReciente(limite);
    }

    @GetMapping("/traducciones/conteo")
    @Operation(summary = "Contar por modo de salida", description = "Prueba modo=texto o modo=voz")
    public Map<String, Object> contarPorModo(@RequestParam String modo) {
        return Map.of("modo", modo, "total", repositorio.contarTraduccionesPorModo(modo));
    }

    @GetMapping("/traducciones/confianza-promedio")
    @Operation(summary = "Promedio de confianza del modelo")
    public Map<String, Double> promedioConfianza() {
        return Map.of("promedio", repositorio.promedioConfianza());
    }
}
