package com.agtech.nepenya.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.UUID;

/**
 * Entidad que representa un usuario del sistema.
 * Almacena informacion del agricultor y estado de sincronizacion.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Entity(tableName = "usuarios")
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "nombre")
    private String nombre;

    @ColumnInfo(name = "telefono")
    private String telefono;

    @ColumnInfo(name = "sync_status", defaultValue = "PENDING")
    private String syncStatus;

    @ColumnInfo(name = "remote_id")
    private Integer remoteId;

    @ColumnInfo(name = "uuid")
    private String uuid;

    @ColumnInfo(name = "firebase_uid")
    private String firebaseUid;

    /**
     * Constructor vacio requerido por Room.
     */
    public Usuario() {
        this.syncStatus = "PENDING";
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Constructor con parametros esenciales.
     *
     * @param nombre    Nombre completo del usuario
     * @param telefono  Numero de telefono
     */
    @Ignore
    public Usuario(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
