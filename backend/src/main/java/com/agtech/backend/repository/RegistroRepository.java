package com.agtech.backend.repository;

import com.agtech.backend.model.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA para Registro.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Repository
public interface RegistroRepository extends JpaRepository<Registro, Long> {
    List<Registro> findByParcelaId(Long parcelaId);
    List<Registro> findByTipo(String tipo);
    java.util.Optional<Registro> findByUuid(String uuid);
    List<Registro> findByParcelaUsuarioFirebaseUid(String firebaseUid);
}
