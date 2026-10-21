package org.example.lsc.repository;

import java.util.List;
import org.example.lsc.entity.Traduccion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TraduccionRepository extends JpaRepository<Traduccion, Long> {

    List<Traduccion> findAllByOrderByCreadoEnDescIdDesc(Pageable pageable);

    long countByModoIgnoreCase(String modo);

    @Query("select avg(t.confianza) from Traduccion t")
    Double promedioConfianza();
}
