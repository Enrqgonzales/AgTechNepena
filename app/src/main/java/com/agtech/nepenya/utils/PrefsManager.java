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
    public static final String KEY_CURRENCY_BASE = "currency_base";
    public static final String KEY_CURRENCY_RATES = "currency_rates";
    public static final String KEY_CURRENCY_INDEX = "currency_index";
    public static final String KEY_SERVER_IP = "server_ip";
    public static final String KEY_ADMIN_PIN = "admin_pin";

    // Defaults
    private static final int DEFAULT_FONT_SIZE = 16; // Centro del rango 12-32
    private static final float DEFAULT_BRIGHTNESS = 0.5f;
    private static final String DEFAULT_THEME_MODE = "DIA";
    private static final boolean DEFAULT_VOICE_ENABLED = true;

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

    // Currency Index (0=PEN, 1=EUR, 2=GBP, 3=JPY, 4=CNY)

    public int getCurrencyIndex() {
        return prefs.getInt(KEY_CURRENCY_INDEX, 0);
    }

    public void setCurrencyIndex(int index) {
        prefs.edit().putInt(KEY_CURRENCY_INDEX, index).apply();
    }

    // Server IP

    public String getServerIp() {
        return prefs.getString(KEY_SERVER_IP, "");
    }

    public void setServerIp(String ip) {
        prefs.edit().putString(KEY_SERVER_IP, ip).apply();
    }

    // Currency Base

    public String getCurrencyBase() {
        return prefs.getString(KEY_CURRENCY_BASE, "USD");
    }

    public void setCurrencyBase(String currency) {
        prefs.edit().putString(KEY_CURRENCY_BASE, currency).apply();
    }

    // Currency Rates JSON

    public String getCurrencyRates() {
        return prefs.getString(KEY_CURRENCY_RATES, "");
    }

    public void setCurrencyRates(String ratesJson) {
        prefs.edit().putString(KEY_CURRENCY_RATES, ratesJson)
                .putLong(KEY_CAMBIO_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    /**
     * Obtiene el valor de una tasa específica del JSON de tasas.
     * Parsea el JSON almacenado y extrae el valor numérico.
     *
     * @param currencyCode Código de moneda (USD, EUR, PEN, etc.)
     * @return Valor de la tasa, o 1.0 si no está disponible
     */
    public double getCurrencyRate(String currencyCode) {
        String ratesJson = getCurrencyRates();
        if (ratesJson == null || ratesJson.isEmpty()) {
            return 1.0;
        }
        try {
            org.json.JSONObject json = new org.json.JSONObject(ratesJson);
            if (json.has(currencyCode)) {
                return json.getDouble(currencyCode);
            }
            return 1.0;
        } catch (org.json.JSONException e) {
            return 1.0;
        }
    }

    // Admin PIN Management

    public String getAdminPin() {
        return prefs.getString(KEY_ADMIN_PIN, null);
    }

    public void setAdminPin(String pin) {
        if (pin == null) {
            prefs.edit().remove(KEY_ADMIN_PIN).apply();
        } else {
            String hashed = hashSHA256(pin);
            prefs.edit().putString(KEY_ADMIN_PIN, hashed).apply();
        }
    }

    public boolean verificarAdminPin(String pin) {
        String guardado = getAdminPin();
        if (guardado == null) return false;
        return guardado.equals(hashSHA256(pin));
    }

    private String hashSHA256(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Limpia todas las preferencias.
     */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
