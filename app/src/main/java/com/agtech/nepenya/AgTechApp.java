package com.agtech.nepenya;

import android.app.Application;
import android.os.Bundle;

import com.agtech.nepenya.sync.SyncManager;

/**
 * Clase Application principal de AgTech Nepeña.
 * Inicializa servicios globales al arrancar la app.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class AgTechApp extends Application {

    private static int startedActivities = 0;
    private static boolean requireAppUnlock = true;

    @Override
    public void onCreate() {
        super.onCreate();
        SyncManager.startPeriodicSync(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(android.app.Activity activity) {
                startedActivities++;
            }

            @Override
            public void onActivityStopped(android.app.Activity activity) {
                startedActivities--;
                if (startedActivities <= 0) {
                    startedActivities = 0;
                    requireAppUnlock = true;
                }
            }

            @Override
            public void onActivityCreated(android.app.Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityResumed(android.app.Activity activity) {
            }

            @Override
            public void onActivityPaused(android.app.Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(android.app.Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(android.app.Activity activity) {
            }
        });
    }

    public static boolean shouldRequireAppUnlock() {
        return requireAppUnlock;
    }

    public static void markAppUnlocked() {
        requireAppUnlock = false;
    }
}
