package com.agtech.nepenya.model.repository;

import com.agtech.nepenya.model.dao.UsuarioDao;
import com.agtech.nepenya.model.entity.Usuario;

import java.util.List;

/**
 * Repository para operaciones de Usuario.
 * Actua como fuente unica de verdad entre Controller y DAO.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class UsuarioRepository {

    private final UsuarioDao usuarioDao;

    /**
     * Constructor con inyeccion de DAO.
     *
     * @param usuarioDao DAO de usuario
     */
    public UsuarioRepository(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    /**
     * Inserta un nuevo usuario.
     *
     * @param usuario Usuario a insertar
     * @return ID generado
     */
    public long insertar(Usuario usuario) {
        return usuarioDao.insertar(usuario);
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param usuario Usuario a actualizar
     */
    public void actualizar(Usuario usuario) {
        usuarioDao.actualizar(usuario);
    }

    /**
     * Elimina un usuario.
     *
     * @param usuario Usuario a eliminar
     */
    public void eliminar(Usuario usuario) {
        usuarioDao.eliminar(usuario);
    }

    /**
     * Obtiene todos los usuarios.
     *
     * @return Lista de usuarios
     */
    public List<Usuario> obtenerTodos() {
        return usuarioDao.obtenerTodos();
    }

    /**
     * Obtiene un usuario por ID.
     *
     * @param id Identificador del usuario
     * @return Usuario encontrado o null
     */
    public Usuario obtenerPorId(int id) {
        return usuarioDao.obtenerPorId(id);
    }

    /**
     * Obtiene usuarios pendientes de sincronizacion.
     *
     * @return Lista de usuarios pendientes
     */
    public List<Usuario> obtenerPendientesSync() {
        return usuarioDao.obtenerPendientesSync();
    }

    /**
     * Cuenta usuarios pendientes de sincronizacion.
     *
     * @return Cantidad de usuarios pendientes
     */
    public int contarPendientes() {
        return usuarioDao.contarPendientes();
    }

    /**
     * Actualiza estado de sincronizacion.
     *
     * @param id         ID del usuario
     * @param syncStatus Nuevo estado
     * @param remoteId   ID remoto
     */
    public void actualizarSyncStatus(int id, String syncStatus, Integer remoteId) {
        usuarioDao.actualizarSyncStatus(id, syncStatus, remoteId);
    }
}
