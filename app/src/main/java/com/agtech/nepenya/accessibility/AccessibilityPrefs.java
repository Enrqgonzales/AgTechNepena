package com.agtech.nepenya.accessibility;

import android.app.Activity;
import android.view.WindowManager;
import android.util.TypedValue;

import com.agtech.nepenya.utils.PrefsManager;

/**
 * Aplica preferencias de accesibilidad a Activities.
 * Gestiona fuente, brillo y tema.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class AccessibilityPrefs {

    /**
     * Aplica todas las preferencias de accesibilidad a una Activity.
     * Debe llamarse antes de setContentView().
     *
     * @param activity Activity a configurar
     */
    public static void applyAll(Activity activity) {
        PrefsManager prefs = new PrefsManager(activity);

        // Aplicar tema
        applyTheme(activity, prefs.getThemeMode());

        // Aplicar brillo
        applyBrightness(activity, prefs.getBrightness());
    }

    /**
     * Aplica tema Dia o Noche.
     *
     * @param activity Activity
     * @param themeMode "DIA" o "NOCHE"
     */
    public static void applyTheme(Activity activity, String themeMode) {
        if ("NOCHE".equals(themeMode)) {
            // En modo noche, usar tema oscuro
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            // En modo dia, usar tema claro
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    /**
     * Aplica nivel de brillo a la ventana.
     *
     * @param activity  Activity
     * @param brightness Nivel 0.0 - 1.0
     */
    public static void applyBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = brightness;
        activity.getWindow().setAttributes(layoutParams);
    }

    /**
     * Convierte SP a pixeles.
     *
     * @param activity Activity
     * @param sp       Valor en SP
     * @return Valor en pixeles
     */
    public static float spToPx(Activity activity, float sp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, activity.getResources().getDisplayMetrics());
    }
}
