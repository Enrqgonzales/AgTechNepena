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

    @Query("SELECT * FROM inventario WHERE parcela_id = :parcelaId ORDER BY nombre ASC")
    List<InventarioItem> obtenerPorParcela(int parcelaId);

    @Query("SELECT * FROM inventario WHERE parcela_id = :parcelaId AND categoria = :categoria ORDER BY nombre ASC")
    List<InventarioItem> obtenerPorParcelaYCategoria(int parcelaId, String categoria);

    @Query("SELECT * FROM inventario WHERE parcela_id = :parcelaId AND nombre = :nombre AND categoria = :categoria LIMIT 1")
    InventarioItem obtenerPorParcelaYNombreCategoria(int parcelaId, String nombre, String categoria);

    @Query("SELECT * FROM inventario WHERE parcela_id = :parcelaId AND categoria = :categoria AND unidad = :unidad LIMIT 1")
    InventarioItem obtenerPorParcelaCategoriaUnidad(int parcelaId, String categoria, String unidad);

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
    Double obtenerValorTotalInventario();

    @Query("SELECT SUM(cantidad) FROM inventario WHERE categoria = :categoria")
    double obtenerCantidadTotalPorCategoria(String categoria);

    @Query("SELECT COUNT(*) FROM inventario")
    int contarItems();

    @Query("SELECT COUNT(*) FROM inventario WHERE cantidad > 0")
    int contarItemsConStock();

    // Sincronización

    @Query("SELECT * FROM inventario WHERE sync_status = 'PENDING' OR sync_status IS NULL")
    List<InventarioItem> obtenerPendientesSync();

    @Query("UPDATE inventario SET sync_status = :syncStatus, remote_id = :remoteId WHERE id = :id")
    void actualizarSyncStatus(int id, String syncStatus, int remoteId);
}
