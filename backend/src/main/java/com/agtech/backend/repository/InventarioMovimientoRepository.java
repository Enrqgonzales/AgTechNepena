package com.agtech.backend.repository;

import com.agtech.backend.model.InventarioMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para InventarioMovimiento.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {
    java.util.Optional<InventarioMovimiento> findByUuid(String uuid);
}
