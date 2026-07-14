package com.agtech.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para InventarioMovimiento (Entradas/Salidas de insumos).
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Entity
@Table(name = "inventario_movimientos")
public class InventarioMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private InventarioItem inventarioItem;

    @Column(nullable = false)
    private String tipo; // ENTRADA o SALIDA

    private Double cantidad;

    private String unidad;

    @Column(name = "costo_total")
    private Double costoTotal;

    private String fecha;

    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "registro_id")
    private Registro registro; // Relación opcional con el gasto asociado

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (uuid == null) {
            uuid = java.util.UUID.randomUUID().toString();
        }
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InventarioItem getInventarioItem() {
        return inventarioItem;
    }

    public void setInventarioItem(InventarioItem inventarioItem) {
        this.inventarioItem = inventarioItem;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(Double costoTotal) {
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

    public Registro getRegistro() {
        return registro;
    }

    public void setRegistro(Registro registro) {
        this.registro = registro;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
