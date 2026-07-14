package com.agtech.backend.repository;

import com.agtech.backend.model.InventarioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para InventarioItem.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Repository
public interface InventarioRepository extends JpaRepository<InventarioItem, Long> {
    java.util.Optional<InventarioItem> findByUuid(String uuid);
}
