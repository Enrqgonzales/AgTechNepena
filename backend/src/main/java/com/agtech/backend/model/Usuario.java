package com.agtech.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para Usuario.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "uuid", unique = true)
    private String uuid;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
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
}
