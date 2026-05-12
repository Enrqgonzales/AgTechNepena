package com.agtech.nepenya.accessibility;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.WindowManager;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;

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

        // Aplicar escala de fuente
        applyFontScale(activity, prefs.getFontSize());

        // Aplicar brillo
        applyBrightness(activity, prefs.getBrightness());
    }

    /**
     * Aplica tema Dia o Noche.
     * Solo actualiza el modo global sin recrear la Activity.
     * Llamar activity.recreate() manualmente si se necesita aplicar en caliente.
     *
     * @param activity  Activity
     * @param themeMode "DIA" o "NOCHE"
     */
    public static void applyTheme(Activity activity, String themeMode) {
        int newMode = "NOCHE".equals(themeMode)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;

        AppCompatDelegate.setDefaultNightMode(newMode);
    }

    /**
     * Obtiene el modo nocturno actual.
     *
     * @return true si está en modo noche
     */
    public static boolean isNightMode(Activity activity) {
        return (activity.getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Aplica nivel de brillo a la ventana.
     *
     * @param activity   Activity
     * @param brightness Nivel 0.0 - 1.0
     */
    public static void applyBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = brightness;
        activity.getWindow().setAttributes(layoutParams);
    }

    /**
     * Aplica escala de fuente global a la Activity.
     * La escala se calcula relativa al tamanio base de 16sp.
     *
     * @param activity Activity
     * @param fontSize Tamanio en SP (rango 12-32)
     */
    public static void applyFontScale(Activity activity, int fontSize) {
        float fontScale = fontSize / 16f;
        Configuration config = new Configuration(activity.getResources().getConfiguration());
        config.fontScale = fontScale;
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
    }

    public static float spToPx(Activity activity, float sp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, activity.getResources().getDisplayMetrics());
    }
}
