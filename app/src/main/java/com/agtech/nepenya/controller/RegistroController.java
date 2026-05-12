package com.agtech.nepenya.controller;

import android.app.Activity;

import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.model.repository.RegistroRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller para la pantalla de Registro.
 * Gestiona creacion de gastos e ingresos.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class RegistroController {

    private final Activity activity;
    private final ParcelaRepository parcelaRepository;
    private final RegistroRepository registroRepository;
    private final ExecutorService executorService;

    /**
     * Callback para carga de parcelas.
     */
    public interface ParcelasCallback {
        void onParcelasCargadas(List<Parcela> parcelas);
        void onError(String mensaje);
    }

    /**
     * Callback para guardado de registro.
     */
    public interface GuardarCallback {
        void onSuccess();
        void onError(String mensaje);
    }

    /**
     * Callback para validacion.
     */
    public interface ValidacionCallback {
        void onValido();
        void onInvalido(String mensajeError);
    }

    // Categorias predefinidas
    public static final String[] CATEGORIAS_GASTO = {
            "Fertilizantes", "Mano de Obra", "Combustible", "Mantenimiento Riego",
            "Pesticidas", "Siembra", "Cosecha", "Transporte", "Otros"
    };

    public static final String[] CATEGORIAS_INGRESO = {
            "Venta Lote A", "Venta Lote B", "Venta Lote C",
            "Venta Cosecha", "Subsidio", "Otros"
    };

    /**
     * Constructor con inyeccion de dependencias.
     */
    public RegistroController(Activity activity,
                              ParcelaRepository parcelaRepository,
                              RegistroRepository registroRepository) {
        this.activity = activity;
        this.parcelaRepository = parcelaRepository;
        this.registroRepository = registroRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Carga todas las parcelas disponibles.
     *
     * @param callback Callback con lista de parcelas
     */
    public void cargarParcelas(ParcelasCallback callback) {
        executorService.execute(() -> {
            List<Parcela> parcelas = parcelaRepository.obtenerTodas();
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
     * Valida campos del formulario.
     *
     * @param parcelaId ID de parcela seleccionada
     * @param tipo      Tipo de registro
     * @param categoria Categoria seleccionada
     * @param monto     Monto ingresado
     * @param fecha     Fecha seleccionada
     * @param callback  Callback de validacion
     */
    public void validarCampos(int parcelaId, String tipo, String categoria,
                              double monto, String fecha, ValidacionCallback callback) {
        executorService.execute(() -> {
            String error = null;

            if (parcelaId <= 0) {
                error = "Seleccione una parcela";
            } else if (tipo == null || tipo.isEmpty()) {
                error = "Seleccione tipo de registro";
            } else if (categoria == null || categoria.isEmpty()) {
                error = "Seleccione una categoria";
            } else if (monto <= 0) {
                error = "Ingrese un monto valido";
            } else if (fecha == null || fecha.isEmpty()) {
                error = "Seleccione una fecha";
            }

            final String mensajeError = error;
            activity.runOnUiThread(() -> {
                if (mensajeError == null) {
                    callback.onValido();
                } else {
                    callback.onInvalido(mensajeError);
                }
            });
        });
    }

    /**
     * Guarda un nuevo registro.
     *
     * @param parcelaId   ID de parcela
     * @param tipo        Tipo (GASTO/INGRESO)
     * @param categoria   Categoria
     * @param monto       Monto
     * @param fecha       Fecha
     * @param descripcion Descripcion opcional
     * @param callback    Callback de resultado
     */
    public void guardarRegistro(int parcelaId, String tipo, String categoria,
                                double monto, String fecha, String descripcion,
                                GuardarCallback callback) {
        executorService.execute(() -> {
            Registro registro = new Registro();
            registro.setParcelaId(parcelaId);
            registro.setTipo(tipo);
            registro.setCategoria(categoria);
            registro.setMonto(monto);
            registro.setFecha(fecha);
            registro.setDescripcion(descripcion != null ? descripcion : "");
            registro.setSyncStatus("PENDING");

            registroRepository.insertar(registro);

            activity.runOnUiThread(callback::onSuccess);
        });
    }

    /**
     * Obtiene categorias segun tipo de registro.
     *
     * @param tipo Tipo de registro
     * @return Array de categorias
     */
    public String[] obtenerCategorias(String tipo) {
        if ("INGRESO".equals(tipo)) {
            return CATEGORIAS_INGRESO;
        }
        return CATEGORIAS_GASTO;
    }

    /**
     * Formatea monto para mostrar.
     *
     * @param monto Monto en soles
     * @return String formateado
     */
    public String formatearMonto(double monto) {
        return String.format(Locale.getDefault(), "S/ %.2f", monto);
    }

    /**
     * Obtiene fecha actual formateada.
     *
     * @return Fecha en formato yyyy-MM-dd
     */
    public String obtenerFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * Obtiene año actual.
     *
     * @return Año en formato yyyy
     */
    public String obtenerAnioActual() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }
}
