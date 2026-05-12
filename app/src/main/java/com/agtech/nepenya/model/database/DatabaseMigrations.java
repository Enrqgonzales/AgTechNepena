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
                            "remoteId INTEGER NOT NULL)");

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
                            "FOREIGN KEY(item_id) REFERENCES inventario(id) ON UPDATE NO ACTION ON DELETE CASCADE)");

            // Crear índices para mejor performance
            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_inventario_movimientos_item_id " +
                            "ON inventario_movimientos(item_id)");
            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_inventario_movimientos_fecha " +
                            "ON inventario_movimientos(fecha)");
        }
    };

    /**
     * Migración de versión 2 a 3:
     * Agrega parcela_id a tabla inventario con ForeignKey a parcelas
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. Agregar columna parcela_id (sin FK constraint - SQLite no permite en
            // ALTER)
            database.execSQL("ALTER TABLE inventario ADD COLUMN parcela_id INTEGER NOT NULL DEFAULT 0");

            // 2. Crear índice para parcela_id (necesario para ForeignKey y performance)
            database.execSQL("CREATE INDEX IF NOT EXISTS index_inventario_parcela_id ON inventario(parcela_id)");

            // Nota: Para agregar la ForeignKey real, necesitariamos recrear la tabla
            // completa.
            // Room manejara esto automaticamente al usar @Entity con foreignKeys =
            // @ForeignKey(...).
            // Los datos existentes tendran parcela_id = 0 (sin parcela asignada).
        }
    };

    /**
     * Migración de versión 3 a 4:
     * Renombra columnas syncStatus→sync_status y remoteId→remote_id en inventario
     * para consistencia con el resto del esquema.
     */
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // SQLite no soporta RENAME COLUMN antes de 3.25.0 (API 30+).
            // Se recrea la tabla con los nombres correctos y FOREIGN KEY.
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS inventario_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "nombre TEXT, " +
                            "categoria TEXT, " +
                            "cantidad REAL NOT NULL, " +
                            "unidad TEXT, " +
                            "costo_unitario REAL NOT NULL, " +
                            "fecha_ingreso TEXT, " +
                            "descripcion TEXT, " +
                            "sync_status TEXT, " +
                            "remote_id INTEGER NOT NULL, " +
                            "parcela_id INTEGER NOT NULL, " +
                            "FOREIGN KEY(parcela_id) REFERENCES parcelas(id) ON UPDATE NO ACTION ON DELETE CASCADE)");

            // Copiar datos existentes
            database.execSQL(
                    "INSERT INTO inventario_new " +
                            "(id, nombre, categoria, cantidad, unidad, costo_unitario, " +
                            "fecha_ingreso, descripcion, sync_status, remote_id, parcela_id) " +
                            "SELECT id, nombre, categoria, cantidad, unidad, costo_unitario, " +
                            "fecha_ingreso, descripcion, syncStatus, remoteId, parcela_id " +
                            "FROM inventario");

            // Eliminar tabla vieja y renombrar nueva
            database.execSQL("DROP TABLE inventario");
            database.execSQL("ALTER TABLE inventario_new RENAME TO inventario");

            // Recrear índices
            database.execSQL("CREATE INDEX IF NOT EXISTS index_inventario_parcela_id ON inventario(parcela_id)");
        }
    };

    /**
     * Obtiene todas las migraciones definidas.
     */
    public static Migration[] getAllMigrations() {
        return new Migration[] {
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4
        };
    }
}
