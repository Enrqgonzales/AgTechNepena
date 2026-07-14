package com.agtech.nepenya.controller;

import android.content.Context;

import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.entity.InventarioItem;
import com.agtech.nepenya.model.entity.InventarioMovimiento;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.utils.PrefsManager;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller para la pantalla de Bienvenida.
 * Gestiona la creación del usuario y preferencias iniciales.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class BienvenidaController {

    private final Context context;
    private final UsuarioRepository usuarioRepository;
    private final PrefsManager prefsManager;
    private final ExecutorService executorService;

    /**
     * Callback para operaciones de guardado.
     */
    public interface BienvenidaCallback {
        void onUsuarioCreado(int userId);
        void onUsuarioRecuperado(Usuario usuario, String distrito);
        void onError(String mensaje);
    }

    /**
     * Constructor con inyección de dependencias.
     */
    public BienvenidaController(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        this.usuarioRepository = new UsuarioRepository(db.usuarioDao());
        this.prefsManager = new PrefsManager(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Valida los datos de entrada del usuario.
     *
     * @param nombre Nombre del usuario
     * @param distritoPos Posición seleccionada del distrito (0 = no seleccionado)
     * @return Mensaje de error, o null si es válido
     */
    public String validarDatos(String nombre, int distritoPos) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es requerido";
        }
        if (distritoPos == 0) {
            return "Seleccione un distrito";
        }
        return null; // Válido
    }

    /**
     * Crea un nuevo usuario y guarda preferencias iniciales.
     *
     * @param nombre      Nombre del usuario
     * @param telefono    Teléfono del usuario
     * @param distrito    Distrito seleccionado
     * @param firebaseUid UID de Firebase (opcional)
     * @param callback    Callback con resultado
     */
    public void crearUsuario(String nombre, String telefono, String distrito, String firebaseUid,
                             BienvenidaCallback callback) {
        executorService.execute(() -> {
            try {
                Usuario usuario = new Usuario(nombre, telefono);
                usuario.setFirebaseUid(firebaseUid);
                long userId = usuarioRepository.insertar(usuario);

                if (userId > 0) {
                    prefsManager.setUserId((int) userId);
                    prefsManager.setUserName(nombre);
                    prefsManager.setDistrito(distrito);

                    callback.onUsuarioCreado((int) userId);
                } else {
                    callback.onError("Error al crear usuario");
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        });
    }

    /**
     * Busca si el usuario ya existe localmente por Firebase UID.
     */
    public void buscarUsuarioLocal(String firebaseUid, BienvenidaCallback callback) {
        executorService.execute(() -> {
            Usuario usuario = usuarioRepository.obtenerPorFirebaseUid(firebaseUid);
            if (usuario != null) {
                prefsManager.setUserId(usuario.getId());
                prefsManager.setUserName(usuario.getNombre());
                // El distrito no se guarda en la entidad Usuario por ahora,
                // se asume que se recuperara del backend o se pedira.
                callback.onUsuarioRecuperado(usuario, "Desconocido");
            } else {
                callback.onError("No encontrado");
            }
        });
    }

    /**
     * Busca si el usuario existe en la nube por Firebase UID.
     */
    public void buscarUsuarioNube(String firebaseUid, BienvenidaCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String serverIp = prefsManager.getServerIp();
                String urlString;
                
                if (serverIp != null && !serverIp.isEmpty()) {
                    urlString = "http://" + serverIp + ":8080/api/sync/usuarios/firebase/" + firebaseUid;
                } else {
                    urlString = "https://agtechnepena-backend.onrender.com/api/sync/usuarios/firebase/" + firebaseUid;
                }
                
                URL url = java.net.URI.create(urlString).toURL();
                
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-API-Key", "agtech_secret_key_2026");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());
                    
                    // Crear objeto Usuario desde JSON
                    Usuario usuario = new Usuario();
                    usuario.setNombre(json.getString("nombre"));
                    usuario.setTelefono(json.getString("telefono"));
                    usuario.setFirebaseUid(firebaseUid);
                    usuario.setRemoteId(json.getInt("id"));
                    usuario.setSyncStatus("SYNCED");
                    
                    String distrito = json.optString("distrito", "Moro");

                    // Guardar localmente
                    long id = usuarioRepository.insertar(usuario);
                    usuario.setId((int) id);
                    
                    prefsManager.setUserId((int) id);
                    prefsManager.setUserName(usuario.getNombre());
                    prefsManager.setDistrito(distrito);

                    descargarDatosNube(firebaseUid, (int) id, new Runnable() {
                        @Override
                        public void run() {
                            callback.onUsuarioRecuperado(usuario, distrito);
                        }
                    });
                } else {
                    callback.onError("No encontrado en nube");
                }
            } catch (Exception e) {
                callback.onError("Error de red: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void descargarDatosNube(String firebaseUid, int localUserId, Runnable callback) {
        HttpURLConnection conn = null;
        try {
            String serverIp = prefsManager.getServerIp();
            String urlString;
            
            if (serverIp != null && !serverIp.isEmpty()) {
                urlString = "http://" + serverIp + ":8080/api/sync/descargar/" + firebaseUid;
            } else {
                urlString = "https://agtechnepena-backend.onrender.com/api/sync/descargar/" + firebaseUid;
            }
            
            URL url = java.net.URI.create(urlString).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-API-Key", "agtech_secret_key_2026");
            conn.setConnectTimeout(10000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject data = new JSONObject(response.toString());
                
                AppDatabase db = AppDatabase.getInstance(context);
                
                // Mapas de traducción de remoteId -> localId
                Map<Integer, Integer> parcelaMap = new HashMap<>();
                Map<Integer, Integer> registroMap = new HashMap<>();
                Map<Integer, Integer> itemMap = new HashMap<>();

                // 1. Descargar Parcelas
                if (data.has("parcelas")) {
                    JSONArray array = data.getJSONArray("parcelas");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int remoteId = obj.getInt("remoteId");
                        
                        Parcela p = new Parcela();
                        p.setNombre(obj.getString("nombre"));
                        p.setCultivo(obj.getString("cultivo"));
                        p.setHectareas(obj.getDouble("hectareas"));
                        p.setUbicacion(obj.optString("ubicacion", ""));
                        p.setEstado(obj.optString("estado", "DISPONIBLE"));
                        p.setUuid(obj.getString("uuid"));
                        p.setRemoteId(remoteId);
                        p.setSyncStatus("SYNCED");
                        
                        // Buscar si existe local
                        Parcela existing = db.parcelaDao().obtenerPorRemoteId(remoteId);
                        if (existing != null) {
                            p.setId(existing.getId());
                        }
                        p.setUsuarioId(localUserId);
                        
                        long localId = db.parcelaDao().insertar(p);
                        parcelaMap.put(remoteId, (int) localId);
                    }
                }

                // 2. Descargar Registros
                if (data.has("registros")) {
                    JSONArray array = data.getJSONArray("registros");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int remoteId = obj.getInt("remoteId");
                        int parcelaRemoteId = obj.getInt("parcelaRemoteId");
                        
                        Integer localParcelaId = parcelaMap.get(parcelaRemoteId);
                        if (localParcelaId == null) continue;
                        
                        Registro r = new Registro();
                        r.setTipo(obj.getString("tipo"));
                        r.setCategoria(obj.getString("categoria"));
                        r.setMonto(obj.getDouble("monto"));
                        r.setDescripcion(obj.optString("descripcion", ""));
                        r.setFecha(obj.getString("fecha"));
                        r.setUuid(obj.getString("uuid"));
                        r.setRemoteId(remoteId);
                        r.setSyncStatus("SYNCED");
                        r.setParcelaId(localParcelaId);
                        
                        Registro existing = db.registroDao().obtenerPorRemoteId(remoteId);
                        if (existing != null) {
                            r.setId(existing.getId());
                        }
                        
                        long localId = db.registroDao().insertar(r);
                        registroMap.put(remoteId, (int) localId);
                    }
                }

                // 2. Descargar Inventario Items
                if (data.has("inventario")) {
                    JSONArray array = data.getJSONArray("inventario");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int remoteId = obj.getInt("remoteId");
                        int parcelaRemoteId = obj.getInt("parcelaRemoteId");
                        
                        Integer localParcelaId = parcelaMap.get(parcelaRemoteId);
                        if (localParcelaId == null) continue;
                        
                        InventarioItem item = new InventarioItem();
                        item.setNombre(obj.getString("nombre"));
                        item.setCategoria(obj.getString("categoria"));
                        item.setCantidad(obj.getDouble("cantidad"));
                        item.setUnidad(obj.getString("unidad"));
                        item.setCostoUnitario(obj.getDouble("costoUnitario"));
                        item.setFechaIngreso(obj.optString("fechaIngreso", ""));
                        item.setDescripcion(obj.optString("descripcion", ""));
                        item.setUuid(obj.getString("uuid"));
                        item.setRemoteId(remoteId);
                        item.setSyncStatus("SYNCED");
                        item.setParcelaId(localParcelaId);
                        
                        InventarioItem existing = db.inventarioDao().obtenerPorRemoteId(remoteId);
                        if (existing != null) {
                            item.setId(existing.getId());
                        }
                        
                        long localId = db.inventarioDao().insertarItem(item);
                        itemMap.put(remoteId, (int) localId);
                    }
                }

                // 3. Descargar Movimientos
                if (data.has("movimientos")) {
                    JSONArray array = data.getJSONArray("movimientos");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int remoteId = obj.getInt("remoteId");
                        int itemRemoteId = obj.getInt("itemRemoteId");
                        
                        Integer localItemId = itemMap.get(itemRemoteId);
                        if (localItemId == null) continue;
                        
                        InventarioMovimiento m = new InventarioMovimiento();
                        m.setItemId(localItemId);
                        m.setTipo(obj.getString("tipo"));
                        m.setCantidad(obj.getDouble("cantidad"));
                        m.setUnidad(obj.getString("unidad"));
                        m.setCostoTotal(obj.getDouble("costoTotal"));
                        m.setFecha(obj.getString("fecha"));
                        m.setDescripcion(obj.optString("descripcion", ""));
                        m.setUuid(obj.getString("uuid"));
                        m.setRemoteId(remoteId);
                        m.setSyncStatus("SYNCED");
                        
                        if (obj.has("registroRemoteId") && !obj.isNull("registroRemoteId")) {
                            int regRemoteId = obj.getInt("registroRemoteId");
                            Integer localRegId = registroMap.get(regRemoteId);
                            if (localRegId != null) {
                                m.setRegistroId(localRegId);
                            }
                        }
                        
                        InventarioMovimiento existing = db.inventarioMovimientoDao().obtenerPorRemoteId(remoteId);
                        if (existing != null) {
                            m.setId(existing.getId());
                        }
                        
                        db.inventarioMovimientoDao().insertar(m);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
            callback.run();
        }
    }

    /**
     * Libera recursos del controller.
     */
    public void destroy() {
        executorService.shutdown();
    }
}
