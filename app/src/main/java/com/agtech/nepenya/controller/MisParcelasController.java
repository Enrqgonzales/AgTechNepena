package com.agtech.nepenya.controller;

import android.app.Activity;

import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.repository.ParcelaRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller para la pantalla de Mis Parcelas.
 * Gestiona listado, creacion y eliminacion de parcelas.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class MisParcelasController {

    private final Activity activity;
    private final ParcelaRepository parcelaRepository;
    private final ExecutorService executorService;

    public interface ParcelasCallback {
        void onParcelasCargadas(List<Parcela> parcelas);
        void onError(String mensaje);
    }

    public interface GuardarCallback {
        void onSuccess(long id);
        void onError(String mensaje);
    }

    public interface EliminarCallback {
        void onEliminada();
        void onError(String mensaje);
    }

    public MisParcelasController(Activity activity, ParcelaRepository parcelaRepository) {
        this.activity = activity;
        this.parcelaRepository = parcelaRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Carga todas las parcelas del usuario.
     */
    public void cargarParcelas(int usuarioId, ParcelasCallback callback) {
        executorService.execute(() -> {
            List<Parcela> parcelas = parcelaRepository.obtenerPorUsuario(usuarioId);
            activity.runOnUiThread(() -> {
                if (parcelas != null) {
                    callback.onParcelasCargadas(parcelas);
                } else {
                    callback.onError("Error al cargar parcelas");
                }
            });
        });
    }

    /**
     * Guarda una nueva parcela.
     */
    public void guardarParcela(int usuarioId, String nombre, String cultivo,
                               String hectareasStr, String ubicacion, GuardarCallback callback) {
        executorService.execute(() -> {
            String error = null;

            if (nombre == null || nombre.trim().isEmpty()) {
                error = "Ingrese el nombre de la parcela";
            } else if (cultivo == null || cultivo.equals("Seleccionar cultivo")) {
                error = "Seleccione el tipo de cultivo";
            }

            double hectareas = 0;
            if (error == null) {
                try {
                    hectareas = Double.parseDouble(hectareasStr.replace(",", "."));
                    if (hectareas <= 0) error = "Ingrese un valor de hectáreas válido";
                } catch (NumberFormatException e) {
                    error = "Ingrese un valor de hectáreas válido";
                }
            }

            final String mensajeError = error;
            final double hects = hectareas;

            activity.runOnUiThread(() -> {
                if (mensajeError != null) {
                    callback.onError(mensajeError);
                    return;
                }

                Parcela parcela = new Parcela();
                parcela.setUsuarioId(usuarioId);
                parcela.setNombre(nombre.trim());
                parcela.setCultivo(cultivo);
                parcela.setHectareas(hects);
                parcela.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
                parcela.setSyncStatus("PENDING");

                executorService.execute(() -> {
                    long id = parcelaRepository.insertar(parcela);
                    activity.runOnUiThread(() -> callback.onSuccess(id));
                });
            });
        });
    }

    /**
     * Elimina una parcela por ID.
     */
    public void eliminarParcela(Parcela parcela, EliminarCallback callback) {
        executorService.execute(() -> {
            parcelaRepository.eliminar(parcela);
            activity.runOnUiThread(callback::onEliminada);
        });
    }
}
