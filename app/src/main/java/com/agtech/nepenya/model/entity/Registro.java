package com.agtech.nepenya.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.UUID;

/**
 * Entidad que representa un registro de gasto o ingreso.
 * Cada registro esta asociado a una parcela especifica.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Entity(
        tableName = "registros",
        foreignKeys = @ForeignKey(
                entity = Parcela.class,
                parentColumns = "id",
                childColumns = "parcela_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parcela_id"), @Index("fecha"), @Index("tipo")}
)
public class Registro {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "parcela_id")
    private int parcelaId;

    @ColumnInfo(name = "tipo")
    private String tipo;

    @ColumnInfo(name = "categoria")
    private String categoria;

    @ColumnInfo(name = "monto")
    private double monto;

    @ColumnInfo(name = "descripcion")
    private String descripcion;

    @ColumnInfo(name = "fecha")
    private String fecha;

    @ColumnInfo(name = "sync_status", defaultValue = "PENDING")
    private String syncStatus;

    @ColumnInfo(name = "remote_id")
    private Integer remoteId;

    @ColumnInfo(name = "uuid")
    private String uuid;

    /**
     * Constructor vacio requerido por Room.
     */
    public Registro() {
        this.syncStatus = "PENDING";
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Constructor con parametros esenciales.
     *
     * @param parcelaId   Identificador de la parcela asociada
     * @param tipo        Tipo de registro: "GASTO" o "INGRESO"
     * @param categoria   Categoria del registro
     * @param monto       Monto en soles
     * @param descripcion Descripcion detallada
     * @param fecha       Fecha en formato yyyy-MM-dd
     */
    @Ignore
    public Registro(int parcelaId, String tipo, String categoria, double monto, String descripcion, String fecha) {
        this.parcelaId = parcelaId;
        this.tipo = tipo;
        this.categoria = categoria;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.syncStatus = "PENDING";
        this.uuid = UUID.randomUUID().toString();
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParcelaId() {
        return parcelaId;
    }

    public void setParcelaId(int parcelaId) {
        this.parcelaId = parcelaId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return true si es un gasto
     */
    public boolean esGasto() {
        return "GASTO".equals(tipo);
    }

    /**
     * @return true si es un ingreso
     */
    public boolean esIngreso() {
        return "INGRESO".equals(tipo);
    }

    @Override
    public String toString() {
        return "Registro{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", categoria='" + categoria + '\'' +
                ", monto=" + monto +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}
