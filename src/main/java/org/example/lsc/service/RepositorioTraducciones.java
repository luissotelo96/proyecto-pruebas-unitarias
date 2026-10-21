package org.example.lsc.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.example.lsc.entity.Sena;
import org.example.lsc.entity.Traduccion;
import org.example.lsc.repository.SenaRepository;
import org.example.lsc.repository.TraduccionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Módulo de persistencia del traductor LSC: diccionario de señas
 * e historial de traducciones a texto o voz.
 */
@Service
@Transactional
public class RepositorioTraducciones {

    private final SenaRepository senaRepository;
    private final TraduccionRepository traduccionRepository;

    public RepositorioTraducciones(SenaRepository senaRepository, TraduccionRepository traduccionRepository) {
        this.senaRepository = senaRepository;
        this.traduccionRepository = traduccionRepository;
    }

    /**
     * Registra una seña en el diccionario.
     *
     * @param codigo    identificador de la seña, por ejemplo HOLA
     * @param palabra   traducción al español
     * @param categoria grupo: saludo, familia, etc.
     * @return seña guardada
     */
    public Sena registrarSena(String codigo, String palabra, String categoria) {
        String codigoNormalizado = exigirTexto(codigo, "codigo").toUpperCase(Locale.ROOT);
        String palabraNormalizada = exigirTexto(palabra, "palabra").toLowerCase(Locale.ROOT);
        String categoriaNormalizada = exigirTexto(categoria, "categoria").toLowerCase(Locale.ROOT);

        if (senaRepository.existsByCodigoIgnoreCase(codigoNormalizado)) {
            throw new IllegalArgumentException("La seña " + codigoNormalizado + " ya existe");
        }

        return senaRepository.save(new Sena(codigoNormalizado, palabraNormalizada, categoriaNormalizada));
    }

    /**
     * Busca la palabra en español asociada a un código de seña.
     */
    @Transactional(readOnly = true)
    public Optional<String> buscarPalabra(String codigo) {
        String codigoNormalizado = exigirTexto(codigo, "codigo").toUpperCase(Locale.ROOT);
        return senaRepository.findByCodigoIgnoreCase(codigoNormalizado).map(Sena::getPalabra);
    }

    /**
     * Lista las señas de una categoría, ordenadas por palabra.
     */
    @Transactional(readOnly = true)
    public List<Sena> listarSenasPorCategoria(String categoria) {
        String categoriaNormalizada = exigirTexto(categoria, "categoria").toLowerCase(Locale.ROOT);
        return senaRepository.findByCategoriaIgnoreCaseOrderByPalabraAsc(categoriaNormalizada);
    }

    /**
     * Guarda una traducción realizada por el sistema (texto o voz).
     *
     * @param codigo     seña reconocida
     * @param texto      palabra o frase generada
     * @param modo       "texto" o "voz"
     * @param confianza  valor entre 0.0 y 1.0
     */
    public Traduccion guardarTraduccion(String codigo, String texto, String modo, double confianza) {
        String codigoNormalizado = exigirTexto(codigo, "codigo").toUpperCase(Locale.ROOT);
        String textoNormalizado = exigirTexto(texto, "texto");
        String modoNormalizado = exigirTexto(modo, "modo").toLowerCase(Locale.ROOT);

        if (!modoNormalizado.equals("texto") && !modoNormalizado.equals("voz")) {
            throw new IllegalArgumentException("El modo debe ser texto o voz");
        }
        if (confianza < 0 || confianza > 1) {
            throw new IllegalArgumentException("La confianza debe estar entre 0 y 1");
        }
        if (senaRepository.findByCodigoIgnoreCase(codigoNormalizado).isEmpty()) {
            throw new IllegalArgumentException("No existe la seña " + codigoNormalizado);
        }

        return traduccionRepository.save(
                new Traduccion(codigoNormalizado, textoNormalizado, modoNormalizado, confianza)
        );
    }

    /**
     * Devuelve las últimas traducciones, de la más reciente a la más antigua.
     */
    @Transactional(readOnly = true)
    public List<Traduccion> listarHistorialReciente(int limite) {
        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser mayor que cero");
        }
        return traduccionRepository.findAllByOrderByCreadoEnDescIdDesc(PageRequest.of(0, limite));
    }

    /**
     * Cuenta cuántas traducciones se hicieron en un modo (texto o voz).
     */
    @Transactional(readOnly = true)
    public long contarTraduccionesPorModo(String modo) {
        String modoNormalizado = exigirTexto(modo, "modo").toLowerCase(Locale.ROOT);
        if (!modoNormalizado.equals("texto") && !modoNormalizado.equals("voz")) {
            throw new IllegalArgumentException("El modo debe ser texto o voz");
        }
        return traduccionRepository.countByModoIgnoreCase(modoNormalizado);
    }

    /**
     * Promedio de confianza de todas las traducciones guardadas.
     * Si no hay registros, devuelve 0.0.
     */
    @Transactional(readOnly = true)
    public double promedioConfianza() {
        Double promedio = traduccionRepository.promedioConfianza();
        return promedio == null ? 0.0 : Math.round(promedio * 100.0) / 100.0;
    }

    /**
     * Elimina una seña del diccionario.
     *
     * @return true si existía y se eliminó
     */
    public boolean eliminarSena(String codigo) {
        String codigoNormalizado = exigirTexto(codigo, "codigo").toUpperCase(Locale.ROOT);
        if (!senaRepository.existsByCodigoIgnoreCase(codigoNormalizado)) {
            throw new IllegalArgumentException("No existe la seña " + codigoNormalizado);
        }
        senaRepository.deleteByCodigoIgnoreCase(codigoNormalizado);
        return true;
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }
}
