package com.agtech.nepenya.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.agtech.nepenya.model.dao.InventarioMovimientoDao;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.InventarioItem;
import com.agtech.nepenya.model.entity.InventarioMovimiento;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.model.repository.InventarioRepository;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Worker para sincronizacion en segundo plano.
 * Sube datos pendientes al servidor cuando hay conexion.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class SyncWorker extends Worker {

    // URL base del backend en producción (Render)
    private static final String BASE_URL = "https://agtechnepena-backend.onrender.com";
    private static final String API_PATH = "/api/sync";

    // IP del emulador Android apuntando al host local
    private static final String EMULATOR_IP = "10.0.2.2";

    // IP de producción por defecto
    private static final String DEFAULT_IP = "192.168.1.100";

    private final Context context;
    private final UsuarioRepository usuarioRepository;
    private final ParcelaRepository parcelaRepository;
    private final RegistroRepository registroRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioMovimientoDao inventarioMovimientoDao;

    /**
     * Obtiene la URL del servidor.
     * Prioriza la URL de producción, pero permite sobrescribirla si hay una IP manual en Prefs.
     */
    private String getBaseUrl() {
        PrefsManager prefs = new PrefsManager(context);
        String serverIp = prefs.getServerIp();
        
        if (serverIp != null && !serverIp.isEmpty()) {
            // Si el usuario configuró una IP manual (ej. para pruebas locales), la usamos
            return "http://" + serverIp + ":8080" + API_PATH;
        }
        
        // Por defecto usamos la URL de producción en Render
        return BASE_URL + API_PATH;
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

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        this.usuarioRepository = new UsuarioRepository(db.usuarioDao());
        this.parcelaRepository = new ParcelaRepository(db.parcelaDao());
        this.registroRepository = new RegistroRepository(db.registroDao());
        this.inventarioRepository = new InventarioRepository(db.inventarioDao());
        this.inventarioMovimientoDao = db.inventarioMovimientoDao();
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

            // Sincronizar inventario
            success &= syncInventario();

            // Sincronizar movimientos
            success &= syncMovimientos();

            if (success) {
                new PrefsManager(context).setLastSync(System.currentTimeMillis());
                return Result.success();
            }
            return Result.retry();

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
                json.put("uuid", u.getUuid());
                json.put("nombre", u.getNombre());
                json.put("telefono", u.getTelefono());
                
                String firebaseUid = u.getFirebaseUid();
                if (firebaseUid == null || firebaseUid.isEmpty()) {
                    com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
                    if (firebaseAuth.getCurrentUser() != null) {
                        firebaseUid = firebaseAuth.getCurrentUser().getUid();
                        u.setFirebaseUid(firebaseUid);
                        usuarioRepository.actualizar(u);
                    }
                }
                json.put("firebaseUid", firebaseUid);
                
                jsonArray.put(json);
            }

            String response = postJson(getBaseUrl() + "/usuarios", jsonArray.toString());
            if (response != null) {
                // Parsear respuesta para obtener remoteId asignado por el servidor
                JSONArray respuestaArray = new JSONArray(response);
                for (int i = 0; i < respuestaArray.length() && i < pendientes.size(); i++) {
                    JSONObject itemRespuesta = respuestaArray.getJSONObject(i);
                    int localId = itemRespuesta.optInt("idLocal", pendientes.get(i).getId());
                    int remoteId = itemRespuesta.optInt("remoteId", localId);
                    usuarioRepository.actualizarSyncStatus(localId, "SYNCED", remoteId);
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
            List<Parcela> aSincronizar = new ArrayList<>();
            for (Parcela p : pendientes) {
                Usuario u = usuarioRepository.obtenerPorId(p.getUsuarioId());
                if (u == null || u.getRemoteId() == null) {
                    continue; // Saltar parcelas de usuarios no sincronizados
                }
                JSONObject json = new JSONObject();
                json.put("idLocal", p.getId());
                json.put("uuid", p.getUuid());
                json.put("usuarioId", u.getRemoteId());
                json.put("nombre", p.getNombre());
                json.put("cultivo", p.getCultivo());
                json.put("hectareas", p.getHectareas());
                json.put("ubicacion", p.getUbicacion());
                json.put("estado", p.getEstado());
                jsonArray.put(json);
                aSincronizar.add(p);
            }

            if (aSincronizar.isEmpty()) {
                return true;
            }

            String response = postJson(getBaseUrl() + "/parcelas", jsonArray.toString());
            if (response != null) {
                // Parsear respuesta para obtener remoteId asignado por el servidor
                JSONArray respuestaArray = new JSONArray(response);
                for (int i = 0; i < respuestaArray.length() && i < aSincronizar.size(); i++) {
                    JSONObject itemRespuesta = respuestaArray.getJSONObject(i);
                    int localId = itemRespuesta.optInt("idLocal", aSincronizar.get(i).getId());
                    int remoteId = itemRespuesta.optInt("remoteId", localId);
                    parcelaRepository.actualizarSyncStatus(localId, "SYNCED", remoteId);
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
            List<Registro> aSincronizar = new ArrayList<>();
            for (Registro r : pendientes) {
                Parcela p = parcelaRepository.obtenerPorId(r.getParcelaId());
                if (p == null || p.getRemoteId() == null) {
                    continue; // Saltar registros de parcelas no sincronizadas
                }
                JSONObject json = new JSONObject();
                json.put("idLocal", r.getId());
                json.put("uuid", r.getUuid());
                json.put("parcelaId", p.getRemoteId());
                json.put("tipo", r.getTipo());
                json.put("categoria", r.getCategoria());
                json.put("monto", r.getMonto());
                json.put("descripcion", r.getDescripcion());
                json.put("fecha", r.getFecha());
                jsonArray.put(json);
                aSincronizar.add(r);
            }

            if (aSincronizar.isEmpty()) {
                return true;
            }

            String response = postJson(getBaseUrl() + "/registros", jsonArray.toString());
            if (response != null) {
                // Parsear respuesta para obtener remoteId asignado por el servidor
                JSONArray respuestaArray = new JSONArray(response);
                for (int i = 0; i < respuestaArray.length() && i < aSincronizar.size(); i++) {
                    JSONObject itemRespuesta = respuestaArray.getJSONObject(i);
                    int localId = itemRespuesta.optInt("idLocal", aSincronizar.get(i).getId());
                    int remoteId = itemRespuesta.optInt("remoteId", localId);
                    registroRepository.actualizarSyncStatus(localId, "SYNCED", remoteId);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean syncInventario() {
        List<InventarioItem> pendientes = inventarioRepository.obtenerPendientesSync();
        if (pendientes.isEmpty()) {
            return true;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            List<InventarioItem> aSincronizar = new ArrayList<>();
            for (InventarioItem item : pendientes) {
                Parcela p = parcelaRepository.obtenerPorId(item.getParcelaId());
                if (p == null || p.getRemoteId() == null) {
                    continue; // Saltar insumos de parcelas no sincronizadas
                }
                JSONObject json = new JSONObject();
                json.put("idLocal", item.getId());
                json.put("uuid", item.getUuid());
                json.put("parcelaId", p.getRemoteId());
                json.put("nombre", item.getNombre());
                json.put("categoria", item.getCategoria());
                json.put("cantidad", item.getCantidad());
                json.put("unidad", item.getUnidad());
                json.put("costoUnitario", item.getCostoUnitario());
                json.put("fechaIngreso", item.getFechaIngreso());
                json.put("descripcion", item.getDescripcion());
                jsonArray.put(json);
                aSincronizar.add(item);
            }

            if (aSincronizar.isEmpty()) {
                return true;
            }

            String response = postJson(getBaseUrl() + "/inventario", jsonArray.toString());
            if (response != null) {
                // Parsear respuesta para obtener remoteId asignado por el servidor
                JSONArray respuestaArray = new JSONArray(response);
                for (int i = 0; i < respuestaArray.length() && i < aSincronizar.size(); i++) {
                    JSONObject itemRespuesta = respuestaArray.getJSONObject(i);
                    int localId = itemRespuesta.optInt("idLocal", aSincronizar.get(i).getId());
                    int remoteId = itemRespuesta.optInt("remoteId", localId);
                    inventarioRepository.actualizarSyncStatus(localId, "SYNCED", remoteId);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean syncMovimientos() {
        List<InventarioMovimiento> pendientes = inventarioMovimientoDao.obtenerPendientesSync();
        if (pendientes.isEmpty()) {
            return true;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            List<InventarioMovimiento> aSincronizar = new ArrayList<>();
            for (InventarioMovimiento mov : pendientes) {
                InventarioItem item = inventarioRepository.obtenerItem(mov.getItemId());
                if (item == null || item.getRemoteId() == 0) {
                    continue; // Saltar si el item de inventario no está sincronizado
                }
                
                Integer remoteRegistroId = null;
                if (mov.getRegistroId() != null) {
                    Registro reg = registroRepository.obtenerPorId(mov.getRegistroId());
                    if (reg != null && reg.getRemoteId() != null) {
                        remoteRegistroId = reg.getRemoteId();
                    } else {
                        continue; // Saltar si el registro asociado existe localmente pero aún no está sincronizado
                    }
                }

                JSONObject json = new JSONObject();
                json.put("idLocal", mov.getId());
                json.put("uuid", mov.getUuid());
                json.put("itemId", item.getRemoteId());
                json.put("tipo", mov.getTipo());
                json.put("cantidad", mov.getCantidad());
                json.put("unidad", mov.getUnidad());
                json.put("costoTotal", mov.getCostoTotal());
                json.put("fecha", mov.getFecha());
                json.put("descripcion", mov.getDescripcion());
                json.put("registroId", remoteRegistroId);
                jsonArray.put(json);
                aSincronizar.add(mov);
            }

            if (aSincronizar.isEmpty()) {
                return true;
            }

            String response = postJson(getBaseUrl() + "/movimientos", jsonArray.toString());
            if (response != null) {
                JSONArray respuestaArray = new JSONArray(response);
                for (int i = 0; i < respuestaArray.length() && i < aSincronizar.size(); i++) {
                    JSONObject itemRespuesta = respuestaArray.getJSONObject(i);
                    int localId = itemRespuesta.optInt("idLocal", aSincronizar.get(i).getId());
                    int remoteId = itemRespuesta.optInt("remoteId", localId);
                    inventarioMovimientoDao.actualizarSyncStatus(localId, "SYNCED", remoteId);
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
            URL url = java.net.URI.create(urlString).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-Key", "agtech_secret_key_2026");
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
