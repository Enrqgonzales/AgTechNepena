package com.agtech.nepenya.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.agtech.nepenya.model.entity.InventarioItem;
import com.agtech.nepenya.model.entity.InventarioMovimiento;

import java.util.List;

/**
 * DAO para operaciones de inventario.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Dao
public interface InventarioDao {

    // CRUD Items

    @Insert
    long insertarItem(InventarioItem item);

    @Update
    void actualizarItem(InventarioItem item);

    @Delete
    void eliminarItem(InventarioItem item);

    @Query("SELECT * FROM inventario WHERE id = :id")
    InventarioItem obtenerItemPorId(int id);

    @Query("SELECT * FROM inventario ORDER BY nombre ASC")
    List<InventarioItem> obtenerTodosItems();

    @Query("SELECT * FROM inventario WHERE categoria = :categoria ORDER BY nombre ASC")
    List<InventarioItem> obtenerPorCategoria(String categoria);

    @Query("SELECT * FROM inventario WHERE cantidad > 0 ORDER BY nombre ASC")
    List<InventarioItem> obtenerItemsConStock();

    @Query("SELECT * FROM inventario WHERE nombre LIKE '%' || :query || '%' ORDER BY nombre ASC")
    List<InventarioItem> buscarItems(String query);

    // Movimientos

    @Insert
    long insertarMovimiento(InventarioMovimiento movimiento);

    @Query("SELECT * FROM inventario_movimientos WHERE item_id = :itemId ORDER BY fecha DESC")
    List<InventarioMovimiento> obtenerMovimientosPorItem(int itemId);

    @Query("SELECT * FROM inventario_movimientos WHERE fecha = :fecha ORDER BY id DESC")
    List<InventarioMovimiento> obtenerMovimientosPorFecha(String fecha);

    @Query("SELECT * FROM inventario_movimientos ORDER BY fecha DESC, id DESC")
    List<InventarioMovimiento> obtenerTodosMovimientos();

    @Query("SELECT * FROM inventario_movimientos WHERE tipo = :tipo ORDER BY fecha DESC")
    List<InventarioMovimiento> obtenerMovimientosPorTipo(String tipo);

    // Estadísticas

    @Query("SELECT SUM(cantidad * costo_unitario) FROM inventario")
    double obtenerValorTotalInventario();

    @Query("SELECT SUM(cantidad) FROM inventario WHERE categoria = :categoria")
    double obtenerCantidadTotalPorCategoria(String categoria);

    @Query("SELECT COUNT(*) FROM inventario")
    int contarItems();

    @Query("SELECT COUNT(*) FROM inventario WHERE cantidad > 0")
    int contarItemsConStock();

    // Sincronización

    @Query("SELECT * FROM inventario WHERE syncStatus = 'PENDING' OR syncStatus IS NULL")
    List<InventarioItem> obtenerPendientesSync();

    @Query("UPDATE inventario SET syncStatus = :syncStatus, remoteId = :remoteId WHERE id = :id")
    void actualizarSyncStatus(int id, String syncStatus, int remoteId);
}
