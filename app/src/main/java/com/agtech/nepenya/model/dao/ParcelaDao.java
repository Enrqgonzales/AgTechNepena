package com.agtech.nepenya.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.agtech.nepenya.model.entity.Parcela;

import java.util.List;

/**
 * Data Access Object para la entidad Parcela.
 * Define operaciones CRUD y consultas especificas.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Dao
public interface ParcelaDao {

    /**
     * Inserta una nueva parcela.
     *
     * @param parcela Parcela a insertar
     * @return ID generado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(Parcela parcela);

    /**
     * Actualiza una parcela existente.
     *
     * @param parcela Parcela a actualizar
     */
    @Update
    void actualizar(Parcela parcela);

    /**
     * Elimina una parcela.
     *
     * @param parcela Parcela a eliminar
     */
    @Delete
    void eliminar(Parcela parcela);

    /**
     * Obtiene todas las parcelas.
     *
     * @return Lista de parcelas
     */
    @Query("SELECT * FROM parcelas ORDER BY nombre ASC")
    List<Parcela> obtenerTodas();

    /**
     * Obtiene parcelas por usuario.
     *
     * @param usuarioId ID del usuario propietario
     * @return Lista de parcelas del usuario
     */
    @Query("SELECT * FROM parcelas WHERE usuario_id = :usuarioId ORDER BY nombre ASC")
    List<Parcela> obtenerPorUsuario(int usuarioId);

    /**
     * Obtiene una parcela por su ID.
     *
     * @param id Identificador de la parcela
     * @return Parcela encontrada o null
     */
    @Query("SELECT * FROM parcelas WHERE id = :id LIMIT 1")
    Parcela obtenerPorId(int id);

    /**
     * Obtiene parcelas pendientes de sincronizacion.
     *
     * @return Lista de parcelas con syncStatus = 'PENDING'
     */
    @Query("SELECT * FROM parcelas WHERE sync_status = 'PENDING'")
    List<Parcela> obtenerPendientesSync();

    /**
     * Obtiene el conteo de parcelas pendientes.
     *
     * @return Cantidad de parcelas pendientes
     */
    @Query("SELECT COUNT(*) FROM parcelas WHERE sync_status = 'PENDING'")
    int contarPendientes();

    @Query("SELECT COUNT(*) FROM parcelas WHERE sync_status = 'PENDING'")
    androidx.lifecycle.LiveData<Integer> contarPendientesLiveData();

    /**
     * Actualiza el estado de sincronizacion.
     *
     * @param id         ID de la parcela
     * @param syncStatus Nuevo estado
     * @param remoteId   ID remoto asignado
     */
    @Query("UPDATE parcelas SET sync_status = :syncStatus, remote_id = :remoteId WHERE id = :id")
    void actualizarSyncStatus(int id, String syncStatus, Integer remoteId);

    /**
     * Obtiene el nombre de una parcela por ID.
     *
     * @param id Identificador de la parcela
     * @return Nombre de la parcela
     */
    @Query("SELECT nombre FROM parcelas WHERE id = :id LIMIT 1")
    String obtenerNombrePorId(int id);

    @Query("SELECT * FROM parcelas WHERE remote_id = :remoteId LIMIT 1")
    Parcela obtenerPorRemoteId(int remoteId);
}
