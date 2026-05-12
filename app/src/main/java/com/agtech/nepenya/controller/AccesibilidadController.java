package com.agtech.nepenya.controller;

import android.app.Activity;
import android.view.WindowManager;

import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.utils.PrefsManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller para la pantalla de Accesibilidad.
 * Gestiona configuraciones de fuente, brillo, tema y asistente de voz.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class AccesibilidadController {

    private final Activity activity;
    private final PrefsManager prefsManager;
    private final ExecutorService executorService;

    /**
     * Callback para operaciones de guardado.
     */
    public interface AccesibilidadCallback {
        void onGuardadoExitoso();
        void onError(String mensaje);
    }

    /**
     * Callback para obtener valores actuales.
     */
    public interface ValoresCallback {
        void onValores(int fontSize, float brightness, String themeMode, boolean voiceEnabled);
    }

    /**
     * Constructor con inyeccion de dependencias.
     */
    public AccesibilidadController(Activity activity, PrefsManager prefsManager) {
        this.activity = activity;
        this.prefsManager = prefsManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Obtiene valores actuales de accesibilidad.
     *
     * @param callback Callback con valores
     */
    public void obtenerValoresActuales(ValoresCallback callback) {
        executorService.execute(() -> {
            int fontSize = prefsManager.getFontSize();
            float brightness = prefsManager.getBrightness();
            String themeMode = prefsManager.getThemeMode();
            boolean voiceEnabled = prefsManager.isVoiceEnabled();

            activity.runOnUiThread(() ->
                    callback.onValores(fontSize, brightness, themeMode, voiceEnabled));
        });
    }

    /**
     * Guarda tamaño de fuente.
     *
     * @param sizeSp   Tamaño en SP (14-28)
     * @param callback Callback de resultado
     */
    public void guardarFuente(int sizeSp, AccesibilidadCallback callback) {
        executorService.execute(() -> {
            prefsManager.setFontSize(sizeSp);
            activity.runOnUiThread(() -> callback.onGuardadoExitoso());
        });
    }

    /**
     * Guarda nivel de brillo y aplica inmediatamente.
     *
     * @param level    Nivel de brillo (0.0 - 1.0)
     * @param callback Callback de resultado
     */
    public void guardarBrillo(float level, AccesibilidadCallback callback) {
        executorService.execute(() -> {
            prefsManager.setBrightness(level);
            aplicarBrillo(level);
            activity.runOnUiThread(() -> callback.onGuardadoExitoso());
        });
    }

    /**
     * Guarda modo de tema y aplica recreando activity.
     *
     * @param modo     Modo: "DIA" o "NOCHE"
     * @param callback Callback de resultado
     */
    public void guardarTema(String modo, AccesibilidadCallback callback) {
        executorService.execute(() -> {
            prefsManager.setThemeMode(modo);
            activity.runOnUiThread(() -> {
                callback.onGuardadoExitoso();
                activity.recreate();
            });
        });
    }

    /**
     * Guarda estado del asistente de voz.
     *
     * @param activo   true para activar
     * @param callback Callback de resultado
     */
    public void guardarVoz(boolean activo, AccesibilidadCallback callback) {
        executorService.execute(() -> {
            prefsManager.setVoiceEnabled(activo);
            activity.runOnUiThread(() -> callback.onGuardadoExitoso());
        });
    }

    /**
     * Aplica configuracion de brillo a la ventana.
     *
     * @param level Nivel de brillo 0.0-1.0
     */
    private void aplicarBrillo(float level) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = level;
        activity.getWindow().setAttributes(layoutParams);
    }

    /**
     * Aplica todas las preferencias de accesibilidad.
     * Llama a AccessibilityPrefs.applyAll()
     */
    public void aplicarTodasLasPreferencias() {
        AccessibilityPrefs.applyAll(activity);
    }
}
