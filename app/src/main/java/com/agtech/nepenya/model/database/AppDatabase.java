package com.agtech.nepenya.model.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.agtech.nepenya.model.dao.InventarioDao;
import com.agtech.nepenya.model.dao.ParcelaDao;
import com.agtech.nepenya.model.dao.RegistroDao;
import com.agtech.nepenya.model.dao.UsuarioDao;
import com.agtech.nepenya.model.entity.InventarioItem;
import com.agtech.nepenya.model.entity.InventarioMovimiento;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.entity.Usuario;

/**
 * Base de datos principal de la aplicacion AgTech Nepeña.
 * Gestiona entidades de Usuario, Parcela y Registro.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
@Database(entities = { Usuario.class, Parcela.class, Registro.class, InventarioItem.class,
        InventarioMovimiento.class }, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "agtech_nepenya_db";
    private static volatile AppDatabase INSTANCE;

    /**
     * Obtiene la instancia singleton de la base de datos.
     *
     * @param context Contexto de la aplicacion
     * @return Instancia de AppDatabase
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .addMigrations(DatabaseMigrations.getAllMigrations())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * @return DAO para operaciones de Usuario
     */
    public abstract UsuarioDao usuarioDao();

    /**
     * @return DAO para operaciones de Parcela
     */
    public abstract ParcelaDao parcelaDao();

    /**
     * @return DAO para operaciones de Registro
     */
    public abstract RegistroDao registroDao();

    /**
     * @return DAO para operaciones de Inventario
     */
    public abstract InventarioDao inventarioDao();
}
