package com.agtech.backend.repository;

import com.agtech.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA para Usuario.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
