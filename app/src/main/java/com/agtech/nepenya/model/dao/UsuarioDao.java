package com.agtech.nepenya.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.agtech.nepenya.model.entity.Usuario;

import java.util.List;

/**
 * Data Access Object para la entidad Usuario.
 * Define operaciones CRUD y consultas especificas.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Dao
public interface UsuarioDao {

    /**
     * Inserta un nuevo usuario.
     *
     * @param usuario Usuario a insertar
     * @return ID generado
     */
    @Insert
    long insertar(Usuario usuario);

    /**
     * Actualiza un usuario existente.
     *
     * @param usuario Usuario a actualizar
     */
    @Update
    void actualizar(Usuario usuario);

    /**
     * Elimina un usuario.
     *
     * @param usuario Usuario a eliminar
     */
    @Delete
    void eliminar(Usuario usuario);

    /**
     * Obtiene todos los usuarios.
     *
     * @return Lista de usuarios
     */
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    List<Usuario> obtenerTodos();

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id Identificador del usuario
     * @return Usuario encontrado o null
     */
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    Usuario obtenerPorId(int id);

    /**
     * Obtiene un usuario por su Firebase UID.
     *
     * @param firebaseUid UID de Firebase
     * @return Usuario encontrado o null
     */
    @Query("SELECT * FROM usuarios WHERE firebase_uid = :firebaseUid LIMIT 1")
    Usuario obtenerPorFirebaseUid(String firebaseUid);

    /**
     * Obtiene usuarios pendientes de sincronizacion.
     *
     * @return Lista de usuarios con syncStatus = 'PENDING'
     */
    @Query("SELECT * FROM usuarios WHERE sync_status = 'PENDING'")
    List<Usuario> obtenerPendientesSync();

    /**
     * Obtiene el conteo de usuarios pendientes.
     *
     * @return Cantidad de usuarios pendientes
     */
    @Query("SELECT COUNT(*) FROM usuarios WHERE sync_status = 'PENDING'")
    int contarPendientes();

    @Query("SELECT COUNT(*) FROM usuarios WHERE sync_status = 'PENDING'")
    androidx.lifecycle.LiveData<Integer> contarPendientesLiveData();

    /**
     * Actualiza el estado de sincronizacion.
     *
     * @param id         ID del usuario
     * @param syncStatus Nuevo estado
     * @param remoteId   ID remoto asignado
     */
    @Query("UPDATE usuarios SET sync_status = :syncStatus, remote_id = :remoteId WHERE id = :id")
    void actualizarSyncStatus(int id, String syncStatus, Integer remoteId);
}
