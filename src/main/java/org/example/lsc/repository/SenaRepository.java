package org.example.lsc.repository;

import java.util.List;
import java.util.Optional;
import org.example.lsc.entity.Sena;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SenaRepository extends JpaRepository<Sena, Long> {

    Optional<Sena> findByCodigoIgnoreCase(String codigo);

    List<Sena> findByCategoriaIgnoreCaseOrderByPalabraAsc(String categoria);

    boolean existsByCodigoIgnoreCase(String codigo);

    void deleteByCodigoIgnoreCase(String codigo);
}
