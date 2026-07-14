package com.agtech.nepenya.model.repository;

import com.agtech.nepenya.model.dao.InventarioDao;
import com.agtech.nepenya.model.entity.InventarioItem;
import com.agtech.nepenya.model.entity.InventarioMovimiento;

import java.util.List;

/**
 * Repository para operaciones de inventario.
 * Gestiona items y movimientos de inventario.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class InventarioRepository {

    private final InventarioDao inventarioDao;

    public InventarioRepository(InventarioDao inventarioDao) {
        this.inventarioDao = inventarioDao;
    }

    // CRUD Items

    public long guardarItem(InventarioItem item) {
        item.setSyncStatus("PENDING");
        if (item.getId() == 0) {
            return inventarioDao.insertarItem(item);
        } else {
            inventarioDao.actualizarItem(item);
            return item.getId();
        }
    }

    public void eliminarItem(InventarioItem item) {
        inventarioDao.eliminarItem(item);
    }

    public InventarioItem obtenerItem(int id) {
        return inventarioDao.obtenerItemPorId(id);
    }

    public List<InventarioItem> obtenerTodos(int userId) {
        return inventarioDao.obtenerTodosItems(userId);
    }

    public List<InventarioItem> obtenerPorCategoria(int userId, String categoria) {
        return inventarioDao.obtenerPorCategoria(userId, categoria);
    }

    public List<InventarioItem> obtenerPorParcela(int parcelaId) {
        return inventarioDao.obtenerPorParcela(parcelaId);
    }

    public List<InventarioItem> obtenerPorParcelaYCategoria(int parcelaId, String categoria) {
        return inventarioDao.obtenerPorParcelaYCategoria(parcelaId, categoria);
    }

    public InventarioItem obtenerPorParcelaYNombreCategoria(int parcelaId, String nombre, String categoria) {
        return inventarioDao.obtenerPorParcelaYNombreCategoria(parcelaId, nombre, categoria);
    }

    public InventarioItem obtenerPorParcelaCategoriaUnidad(int parcelaId, String categoria, String unidad) {
        return inventarioDao.obtenerPorParcelaCategoriaUnidad(parcelaId, categoria, unidad);
    }

    public List<InventarioItem> obtenerConStock() {
        return inventarioDao.obtenerItemsConStock();
    }

    public List<InventarioItem> buscar(String query) {
        return inventarioDao.buscarItems(query);
    }

    // Movimientos

    public long registrarMovimiento(InventarioMovimiento movimiento) {
        return inventarioDao.insertarMovimiento(movimiento);
    }

    public List<InventarioMovimiento> obtenerMovimientosPorItem(int itemId) {
        return inventarioDao.obtenerMovimientosPorItem(itemId);
    }

    public List<InventarioMovimiento> obtenerMovimientosPorFecha(String fecha) {
        return inventarioDao.obtenerMovimientosPorFecha(fecha);
    }

    public List<InventarioMovimiento> obtenerTodosMovimientos() {
        return inventarioDao.obtenerTodosMovimientos();
    }

    // Operaciones complejas

    /**
     * Agrega stock al inventario y registra el movimiento.
     */
    public void agregarStock(int itemId, double cantidad, double costoTotal,
            String fecha, String descripcion) {
        InventarioItem item = obtenerItem(itemId);
        if (item != null) {
            item.agregar(cantidad);
            inventarioDao.actualizarItem(item);

            InventarioMovimiento mov = new InventarioMovimiento(
                    itemId, "ENTRADA", cantidad, item.getUnidad(),
                    costoTotal, fecha, descripcion);
            inventarioDao.insertarMovimiento(mov);
        }
    }

    /**
     * Consume stock del inventario y registra el movimiento.
     * 
     * @return true si se pudo consumir, false si no hay suficiente stock
     */
    public boolean consumirStock(int itemId, double cantidad, double costoTotal,
            String fecha, String descripcion, Integer registroId) {
        InventarioItem item = obtenerItem(itemId);
        if (item != null && item.consumir(cantidad)) {
            item.setSyncStatus("PENDING");
            inventarioDao.actualizarItem(item);

            InventarioMovimiento mov = new InventarioMovimiento(
                    itemId, "SALIDA", cantidad, item.getUnidad(),
                    costoTotal, fecha, descripcion);
            mov.setRegistroId(registroId);
            inventarioDao.insertarMovimiento(mov);
            return true;
        }
        return false;
    }

    // Estadísticas

    public double obtenerValorTotal(int userId) {
        Double total = inventarioDao.obtenerValorTotalInventario(userId);
        return total != null ? total : 0.0;
     }
 
     public int contarItems() {
         return inventarioDao.contarItems();
     }
 
     public int contarItemsConStock(int userId) {
         return inventarioDao.contarItemsConStock(userId);
     }

    // Métodos de sincronización

    /**
     * Obtiene items pendientes de sincronización.
     */
    public List<InventarioItem> obtenerPendientesSync() {
        return inventarioDao.obtenerPendientesSync();
    }

    /**
     * Actualiza el estado de sincronización de un item.
     */
    public void actualizarSyncStatus(int id, String syncStatus, int remoteId) {
        inventarioDao.actualizarSyncStatus(id, syncStatus, remoteId);
    }
}
