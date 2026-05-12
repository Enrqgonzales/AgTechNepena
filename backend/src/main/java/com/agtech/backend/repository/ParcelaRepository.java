package com.agtech.backend.repository;

import com.agtech.backend.model.Parcela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA para Parcela.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    List<Parcela> findByUsuarioId(Long usuarioId);
}
