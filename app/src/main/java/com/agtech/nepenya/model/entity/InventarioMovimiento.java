package com.agtech.nepenya.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.UUID;

/**
 * Entidad que representa un movimiento de inventario (entrada o salida).
 * Registra cuando se agrega o consume un item del inventario.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Entity(
        tableName = "inventario_movimientos",
        foreignKeys = @ForeignKey(
                entity = InventarioItem.class,
                parentColumns = "id",
                childColumns = "item_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("item_id"), @Index("fecha")}
)
public class InventarioMovimiento {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "item_id")
    private int itemId;

    @ColumnInfo(name = "tipo")
    private String tipo; // ENTRADA o SALIDA

    @ColumnInfo(name = "cantidad")
    private double cantidad;

    @ColumnInfo(name = "unidad")
    private String unidad;

    @ColumnInfo(name = "costo_total")
    private double costoTotal;

    @ColumnInfo(name = "fecha")
    private String fecha;

    @ColumnInfo(name = "descripcion")
    private String descripcion;

    @ColumnInfo(name = "registro_id")
    private Integer registroId; // ID del gasto asociado (si aplica)

    @ColumnInfo(name = "uuid")
    private String uuid;

    @ColumnInfo(name = "sync_status", defaultValue = "PENDING")
    private String syncStatus;

    @ColumnInfo(name = "remote_id")
    private Integer remoteId;

    /**
     * Constructor vacio requerido por Room.
     */
    public InventarioMovimiento() {
        this.uuid = UUID.randomUUID().toString();
        this.syncStatus = "PENDING";
    }

    /**
     * Constructor con parametros esenciales.
     */
    @Ignore
    public InventarioMovimiento(int itemId, String tipo, double cantidad, String unidad,
                               double costoTotal, String fecha, String descripcion) {
        this.itemId = itemId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.costoTotal = costoTotal;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.uuid = UUID.randomUUID().toString();
        this.syncStatus = "PENDING";
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Integer registroId) {
        this.registroId = registroId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Integer getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(Integer remoteId) {
        this.remoteId = remoteId;
    }

    public boolean esEntrada() {
        return "ENTRADA".equals(tipo);
    }

    public boolean esSalida() {
        return "SALIDA".equals(tipo);
    }

    @Override
    public String toString() {
        return "InventarioMovimiento{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", tipo='" + tipo + '\'' +
                ", cantidad=" + cantidad +
                '}';
    }
}
