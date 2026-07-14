package com.agtech.nepenya.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.agtech.nepenya.model.entity.InventarioMovimiento;

import java.util.List;

/**
 * DAO para operaciones de persistencia en la tabla inventario_movimientos.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Dao
public interface InventarioMovimientoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(InventarioMovimiento movimiento);

    @Update
    void actualizar(InventarioMovimiento movimiento);

    @Query("SELECT * FROM inventario_movimientos WHERE id = :id")
    InventarioMovimiento obtenerPorId(int id);

    @Query("SELECT * FROM inventario_movimientos WHERE sync_status = 'PENDING'")
    List<InventarioMovimiento> obtenerPendientesSync();

    @Query("SELECT COUNT(*) FROM inventario_movimientos WHERE sync_status = 'PENDING'")
    androidx.lifecycle.LiveData<Integer> contarPendientesLiveData();

    @Query("UPDATE inventario_movimientos SET sync_status = :syncStatus, remote_id = :remoteId WHERE id = :id")
    void actualizarSyncStatus(int id, String syncStatus, Integer remoteId);
}
