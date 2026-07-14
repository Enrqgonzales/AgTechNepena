package com.agtech.nepenya.controller;

import android.content.Context;

import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.utils.PrefsManager;

import org.json.JSONObject;

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
                // Obtener URL base similar a SyncWorker
                String serverIp = prefsManager.getServerIp();
                if (serverIp == null || serverIp.isEmpty()) {
                    // 10.0.2.2 es la IP del host desde el emulador
                    serverIp = "10.0.2.2"; 
                }
                URL url = new URL("http://" + serverIp + ":8080/api/sync/usuarios/firebase/" + firebaseUid);
                
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

                    callback.onUsuarioRecuperado(usuario, distrito);
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

    /**
     * Libera recursos del controller.
     */
    public void destroy() {
        executorService.shutdown();
    }
}
