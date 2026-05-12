package com.agtech.nepenya.controller;

import android.content.Context;

import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Usuario;
import com.agtech.nepenya.model.repository.UsuarioRepository;
import com.agtech.nepenya.utils.PrefsManager;

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
     * @param nombre Nombre del usuario
     * @param telefono Teléfono del usuario
     * @param distrito Distrito seleccionado
     * @param callback Callback con resultado
     */
    public void crearUsuario(String nombre, String telefono, String distrito,
                             BienvenidaCallback callback) {
        executorService.execute(() -> {
            try {
                Usuario usuario = new Usuario(nombre, telefono);
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
     * Libera recursos del controller.
     */
    public void destroy() {
        executorService.shutdown();
    }
}
