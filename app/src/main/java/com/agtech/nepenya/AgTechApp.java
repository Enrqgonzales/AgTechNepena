package com.agtech.nepenya;

import android.app.Application;

import com.agtech.nepenya.sync.SyncManager;

/**
 * Clase Application principal de AgTech Nepeña.
 * Inicializa servicios globales al arrancar la app.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class AgTechApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SyncManager.startPeriodicSync(this);
    }
}
