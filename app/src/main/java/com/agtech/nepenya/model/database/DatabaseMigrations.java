package com.agtech.nepenya.model.database;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Migraciones de base de datos Room.
 * Define cómo migrar datos entre versiones sin perder información.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class DatabaseMigrations {

    /**
     * Migración de versión 1 a 2:
     * Agrega tablas de inventario (inventario e inventario_movimientos)
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Crear tabla inventario con todas las columnas de la entidad
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS inventario (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "nombre TEXT, " +
                "categoria TEXT, " +
                "cantidad REAL NOT NULL, " +
                "unidad TEXT, " +
                "costo_unitario REAL NOT NULL, " +
                "fecha_ingreso TEXT, " +
                "descripcion TEXT, " +
                "syncStatus TEXT, " +
                "remoteId INTEGER NOT NULL)"
            );

            // Crear tabla inventario_movimientos con FK
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS inventario_movimientos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "item_id INTEGER NOT NULL, " +
                "tipo TEXT, " +
                "cantidad REAL NOT NULL, " +
                "unidad TEXT, " +
                "costo_total REAL NOT NULL, " +
                "fecha TEXT, " +
                "descripcion TEXT, " +
                "registro_id INTEGER, " +
                "FOREIGN KEY(item_id) REFERENCES inventario(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
            );

            // Crear índices para mejor performance
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_inventario_movimientos_item_id " +
                "ON inventario_movimientos(item_id)"
            );
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_inventario_movimientos_fecha " +
                "ON inventario_movimientos(fecha)"
            );
        }
    };

    /**
     * Obtiene todas las migraciones definidas.
     */
    public static Migration[] getAllMigrations() {
        return new Migration[] {
            MIGRATION_1_2
        };
    }
}
