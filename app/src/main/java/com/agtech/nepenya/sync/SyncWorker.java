package com.agtech.nepenya.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.model.repository.RegistroRepository;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.utils.NetworkUtils;
import com.agtech.nepenya.utils.PrefsManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Worker para sincronizacion en segundo plano.
 * Sube datos pendientes al servidor cuando hay conexion.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class SyncWorker extends Worker {

    // Puerto y path del backend (la IP se resuelve dinámicamente)
    private static final String SERVER_PORT = "8080";
    private static final String API_PATH = "/api/sync";

    // IP del emulador Android apuntando al host local
    private static final String EMULATOR_IP = "10.0.2.2";

    // IP de producción por defecto
    private static final String DEFAULT_IP = "192.168.1.100";

    private final Context context;
    private final UsuarioRepository usuarioRepository;
    private final ParcelaRepository parcelaRepository;
    private final RegistroRepository registroRepository;

    /**
     * Obtiene la URL del servidor de forma dinámica.
     * Primero intenta la IP guardada en PrefsManager, luego cae al emulador.
     */
    private String getBaseUrl() {
        PrefsManager prefs = new PrefsManager(context);
        String serverIp = prefs.getServerIp();
        if (serverIp == null || serverIp.isEmpty()) {
            serverIp = isRunningOnEmulator() ? EMULATOR_IP : DEFAULT_IP;
        }
        return "http://" + serverIp + ":" + SERVER_PORT + API_PATH;
    }

    /**
     * Detecta si la app corre en un emulador Android.
     */
    private boolean isRunningOnEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK");
    }

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;

        AppDatabase db = AppDatabase.getInstance(context);
        this.usuarioRepository = new UsuarioRepository(db.usuarioDao());
        this.parcelaRepository = new ParcelaRepository(db.parcelaDao());
        this.registroRepository = new RegistroRepository(db.registroDao());
    }

    @NonNull
    @Override
    public Result doWork() {
        // Verificar conectividad
        if (!NetworkUtils.isConnected(context)) {
            return Result.retry();
        }

        try {
            boolean success = true;

            // Sincronizar usuarios
            success &= syncUsuarios();

            // Sincronizar parcelas
            success &= syncParcelas();

            // Sincronizar registros
            success &= syncRegistros();

            return success ? Result.success() : Result.retry();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    private boolean syncUsuarios() {
        List<Usuario> pendientes = usuarioRepository.obtenerPendientesSync();
        if (pendientes.isEmpty()) {
            return true;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            for (Usuario u : pendientes) {
                JSONObject json = new JSONObject();
                json.put("idLocal", u.getId());
                json.put("nombre", u.getNombre());
                json.put("telefono", u.getTelefono());
                jsonArray.put(json);
            }

            String response = postJson(getBaseUrl() + "/usuarios", jsonArray.toString());
            if (response != null) {
                // Actualizar estado de sincronizacion
                for (Usuario u : pendientes) {
                    usuarioRepository.actualizarSyncStatus(u.getId(), "SYNCED", u.getId());
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean syncParcelas() {
        List<Parcela> pendientes = parcelaRepository.obtenerPendientesSync();
        if (pendientes.isEmpty()) {
            return true;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            for (Parcela p : pendientes) {
                JSONObject json = new JSONObject();
                json.put("idLocal", p.getId());
                json.put("usuarioId", p.getUsuarioId());
                json.put("nombre", p.getNombre());
                json.put("cultivo", p.getCultivo());
                json.put("hectareas", p.getHectareas());
                json.put("ubicacion", p.getUbicacion());
                jsonArray.put(json);
            }

            String response = postJson(getBaseUrl() + "/parcelas", jsonArray.toString());
            if (response != null) {
                for (Parcela p : pendientes) {
                    parcelaRepository.actualizarSyncStatus(p.getId(), "SYNCED", p.getId());
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean syncRegistros() {
        List<Registro> pendientes = registroRepository.obtenerPendientesSync();
        if (pendientes.isEmpty()) {
            return true;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            for (Registro r : pendientes) {
                JSONObject json = new JSONObject();
                json.put("idLocal", r.getId());
                json.put("parcelaId", r.getParcelaId());
                json.put("tipo", r.getTipo());
                json.put("categoria", r.getCategoria());
                json.put("monto", r.getMonto());
                json.put("descripcion", r.getDescripcion());
                json.put("fecha", r.getFecha());
                jsonArray.put(json);
            }

            String response = postJson(getBaseUrl() + "/registros", jsonArray.toString());
            if (response != null) {
                for (Registro r : pendientes) {
                    registroRepository.actualizarSyncStatus(r.getId(), "SYNCED", r.getId());
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String postJson(String urlString, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Enviar body
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }
}
