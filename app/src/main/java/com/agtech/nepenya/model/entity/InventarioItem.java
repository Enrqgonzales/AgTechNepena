package com.agtech.nepenya.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad que representa un item en el inventario.
 * Pesticidas, fertilizantes, semillas, etc.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Entity(tableName = "inventario", foreignKeys = @ForeignKey(entity = Parcela.class, parentColumns = "id", childColumns = "parcela_id", onDelete = ForeignKey.CASCADE), indices = {
        @Index("parcela_id") })
public class InventarioItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "nombre")
    private String nombre;

    @ColumnInfo(name = "categoria")
    private String categoria; // PESTICIDA, FERTILIZANTE, SEMILLA, OTRO

    @ColumnInfo(name = "cantidad")
    private double cantidad;

    @ColumnInfo(name = "unidad")
    private String unidad; // KG, LITROS, GALONES, ML, PAQUETES, SACOS

    @ColumnInfo(name = "costo_unitario")
    private double costoUnitario;

    @ColumnInfo(name = "fecha_ingreso")
    private String fechaIngreso;

    @ColumnInfo(name = "descripcion")
    private String descripcion;

    @ColumnInfo(name = "sync_status")
    private String syncStatus = "PENDING";

    @ColumnInfo(name = "remote_id")
    private int remoteId;

    @ColumnInfo(name = "parcela_id")
    private int parcelaId;

    /**
     * Constructor vacio requerido por Room.
     */
    public InventarioItem() {
    }

    /**
     * Constructor con parametros esenciales.
     */
    public InventarioItem(String nombre, String categoria, double cantidad, String unidad,
            double costoUnitario, String fechaIngreso) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.costoUnitario = costoUnitario;
        this.fechaIngreso = fechaIngreso;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public int getParcelaId() {
        return parcelaId;
    }

    public void setParcelaId(int parcelaId) {
        this.parcelaId = parcelaId;
    }

    /**
     * Calcula el costo total del inventario actual.
     */
    public double getCostoTotal() {
        return cantidad * costoUnitario;
    }

    /**
     * Consume una cantidad del inventario.
     * 
     * @param cantidadConsumir cantidad a consumir (debe ser positiva)
     * @return true si se pudo consumir, false si no hay suficiente o cantidad
     *         inválida
     */
    public boolean consumir(double cantidadConsumir) {
        // Validar que la cantidad a consumir sea positiva
        if (cantidadConsumir <= 0) {
            return false;
        }
        // Validar que no consuma más de lo disponible
        if (cantidadConsumir <= cantidad) {
            cantidad -= cantidadConsumir;
            // Validación adicional: asegurar que nunca quede negativo
            if (cantidad < 0) {
                cantidad = 0;
            }
            return true;
        }
        return false;
    }

    /**
     * Agrega cantidad al inventario.
     */
    public void agregar(double cantidadAgregar) {
        cantidad += cantidadAgregar;
    }

    @Override
    public String toString() {
        return "InventarioItem{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cantidad=" + cantidad +
                " " + unidad +
                '}';
    }
}
