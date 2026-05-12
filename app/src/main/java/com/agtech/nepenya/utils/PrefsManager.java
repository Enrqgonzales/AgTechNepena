package com.agtech.nepenya.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manager para preferencias de SharedPreferences.
 * Gestiona configuraciones de usuario persistentes.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class PrefsManager {

    private static final String PREFS_NAME = "AgTechPrefs";

    // Keys
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_BRIGHTNESS = "brightness";
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_VOICE_ENABLED = "voice_enabled";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_LAST_SYNC = "last_sync";
    public static final String KEY_DISTRITO = "distrito";
    public static final String KEY_CLIMA_CACHE = "clima_cache";
    public static final String KEY_CLIMA_TIMESTAMP = "clima_timestamp";
    public static final String KEY_CAMBIO_CACHE = "cambio_cache";
    public static final String KEY_CAMBIO_TIMESTAMP = "cambio_timestamp";
    public static final String KEY_SERVER_IP = "server_ip";

    // Defaults
    private static final int DEFAULT_FONT_SIZE = 16;
    private static final float DEFAULT_BRIGHTNESS = 0.5f;
    private static final String DEFAULT_THEME_MODE = "DIA";
    private static final boolean DEFAULT_VOICE_ENABLED = false;

    private final SharedPreferences prefs;

    /**
     * Constructor.
     *
     * @param context Contexto de la aplicacion
     */
    public PrefsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Font Size

    public int getFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    public void setFontSize(int size) {
        prefs.edit().putInt(KEY_FONT_SIZE, size).apply();
    }

    // Brightness

    public float getBrightness() {
        return prefs.getFloat(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS);
    }

    public void setBrightness(float brightness) {
        prefs.edit().putFloat(KEY_BRIGHTNESS, brightness).apply();
    }

    // Theme Mode

    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE);
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply();
    }

    // Voice Enabled

    public boolean isVoiceEnabled() {
        return prefs.getBoolean(KEY_VOICE_ENABLED, DEFAULT_VOICE_ENABLED);
    }

    public void setVoiceEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply();
    }

    // User ID

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void setUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    // User Name

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    // Last Sync

    public long getLastSync() {
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }

    public void setLastSync(long timestamp) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply();
    }

    // Distrito

    public String getDistrito() {
        return prefs.getString(KEY_DISTRITO, "");
    }

    public void setDistrito(String distrito) {
        prefs.edit().putString(KEY_DISTRITO, distrito).apply();
    }

    // Caché Clima

    public String getClimaCache() {
        return prefs.getString(KEY_CLIMA_CACHE, "");
    }

    public void setClimaCache(String clima) {
        prefs.edit()
                .putString(KEY_CLIMA_CACHE, clima)
                .putLong(KEY_CLIMA_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public long getClimaTimestamp() {
        return prefs.getLong(KEY_CLIMA_TIMESTAMP, 0);
    }

    // Caché Tipo de Cambio

    public String getCambioCache() {
        return prefs.getString(KEY_CAMBIO_CACHE, "");
    }

    public void setCambioCache(String cambio) {
        prefs.edit()
                .putString(KEY_CAMBIO_CACHE, cambio)
                .putLong(KEY_CAMBIO_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public long getCambioTimestamp() {
        return prefs.getLong(KEY_CAMBIO_TIMESTAMP, 0);
    }

    // Server IP

    public String getServerIp() {
        return prefs.getString(KEY_SERVER_IP, "");
    }

    public void setServerIp(String ip) {
        prefs.edit().putString(KEY_SERVER_IP, ip).apply();
    }

    /**
     * Limpia todas las preferencias.
     */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
