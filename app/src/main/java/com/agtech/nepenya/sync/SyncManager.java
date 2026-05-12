package com.agtech.nepenya.sync;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Manager para configuracion de sincronizacion periodica.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class SyncManager {

    private static final String SYNC_WORK_TAG = "agtech_sync_work";
    private static final long SYNC_INTERVAL_MINUTES = 15;

    /**
     * Inicia la sincronizacion periodica.
     *
     * @param context Contexto de la aplicacion
     */
    public static void startPeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, SYNC_INTERVAL_MINUTES,
                TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_TAG,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest);
    }

    /**
     * Detiene la sincronizacion periodica.
     *
     * @param context Contexto de la aplicacion
     */
    public static void stopPeriodicSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_TAG);
    }

    /**
     * Ejecuta sincronizacion una sola vez inmediatamente.
     *
     * @param context Contexto de la aplicacion
     */
    public static void syncNow(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        androidx.work.OneTimeWorkRequest syncWorkRequest = new androidx.work.OneTimeWorkRequest.Builder(
                SyncWorker.class)
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG + "_immediate")
                .build();

        WorkManager.getInstance(context).enqueue(syncWorkRequest);
    }
}
