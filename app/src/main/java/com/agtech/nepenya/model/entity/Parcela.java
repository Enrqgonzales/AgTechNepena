package com.agtech.nepenya.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.UUID;

/**
 * Entidad que representa una parcela agricola.
 * Cada parcela pertenece a un usuario y contiene informacion de cultivo.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Entity(
        tableName = "parcelas",
        foreignKeys = @ForeignKey(
                entity = Usuario.class,
                parentColumns = "id",
                childColumns = "usuario_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("usuario_id")}
)
public class Parcela {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "usuario_id")
    private int usuarioId;

    @ColumnInfo(name = "nombre")
    private String nombre;

    @ColumnInfo(name = "cultivo")
    private String cultivo;

    @ColumnInfo(name = "hectareas")
    private double hectareas;

    @ColumnInfo(name = "ubicacion")
    private String ubicacion;

    @ColumnInfo(name = "sync_status", defaultValue = "PENDING")
    private String syncStatus;

    @ColumnInfo(name = "remote_id")
    private Integer remoteId;

    @ColumnInfo(name = "uuid")
    private String uuid;

    @ColumnInfo(name = "estado", defaultValue = "DISPONIBLE")
    private String estado;

    /**
     * Constructor vacio requerido por Room.
     */
    public Parcela() {
        this.syncStatus = "PENDING";
        this.uuid = UUID.randomUUID().toString();
        this.estado = "DISPONIBLE";
    }

    /**
     * Constructor con parametros esenciales.
     *
     * @param usuarioId Identificador del usuario propietario
     * @param nombre    Nombre de la parcela
     * @param cultivo   Tipo de cultivo (palta, mango, etc.)
     * @param hectareas Extension en hectareas
     * @param ubicacion Ubicacion geografica o referencia
     */
    @Ignore
    public Parcela(int usuarioId, String nombre, String cultivo, double hectareas, String ubicacion) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.cultivo = cultivo;
        this.hectareas = hectareas;
        this.ubicacion = ubicacion;
        this.syncStatus = "PENDING";
        this.uuid = UUID.randomUUID().toString();
        this.estado = "DISPONIBLE";
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCultivo() {
        return cultivo;
    }

    public void setCultivo(String cultivo) {
        this.cultivo = cultivo;
    }

    public double getHectareas() {
        return hectareas;
    }

    public void setHectareas(double hectareas) {
        this.hectareas = hectareas;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Parcela{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cultivo='" + cultivo + '\'' +
                ", hectareas=" + hectareas +
                '}';
    }
}
