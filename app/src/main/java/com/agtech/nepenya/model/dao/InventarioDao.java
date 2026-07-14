package com.agtech.nepenya.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertarItem(InventarioItem item);

    @Update
    void actualizarItem(InventarioItem item);

    @Delete
    void eliminarItem(InventarioItem item);

    @Query("SELECT * FROM inventario WHERE id = :id")
    InventarioItem obtenerItemPorId(int id);

    @Query("SELECT i.* FROM inventario i INNER JOIN parcelas p ON i.parcela_id = p.id WHERE p.usuario_id = :userId ORDER BY i.nombre ASC")
    List<InventarioItem> obtenerTodosItems(int userId);

    @Query("SELECT i.* FROM inventario i INNER JOIN parcelas p ON i.parcela_id = p.id WHERE p.usuario_id = :userId AND i.categoria = :categoria ORDER BY i.nombre ASC")
    List<InventarioItem> obtenerPorCategoria(int userId, String categoria);

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("SELECT SUM(i.cantidad * i.costo_unitario) FROM inventario i INNER JOIN parcelas p ON i.parcela_id = p.id WHERE p.usuario_id = :userId")
    Double obtenerValorTotalInventario(int userId);

    @Query("SELECT SUM(cantidad) FROM inventario WHERE categoria = :categoria")
    double obtenerCantidadTotalPorCategoria(String categoria);

    @Query("SELECT COUNT(*) FROM inventario")
    int contarItems();

    @Query("SELECT COUNT(*) FROM inventario i INNER JOIN parcelas p ON i.parcela_id = p.id WHERE p.usuario_id = :userId AND i.cantidad > 0")
    int contarItemsConStock(int userId);

    // Sincronización

    @Query("SELECT * FROM inventario WHERE sync_status = 'PENDING' OR sync_status IS NULL")
    List<InventarioItem> obtenerPendientesSync();

    @Query("SELECT COUNT(*) FROM inventario WHERE sync_status = 'PENDING' OR sync_status IS NULL")
    androidx.lifecycle.LiveData<Integer> contarPendientesLiveData();

    @Query("UPDATE inventario SET sync_status = :syncStatus, remote_id = :remoteId WHERE id = :id")
    void actualizarSyncStatus(int id, String syncStatus, int remoteId);

    @Query("SELECT * FROM inventario WHERE remote_id = :remoteId LIMIT 1")
    InventarioItem obtenerPorRemoteId(int remoteId);
}
