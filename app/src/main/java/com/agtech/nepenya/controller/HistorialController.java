package com.agtech.nepenya.controller;

import android.app.Activity;

import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.repository.RegistroRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller para la pantalla de Historial.
 * Gestiona listado y eliminacion de registros.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class HistorialController {

    private final Activity activity;
    private final RegistroRepository registroRepository;
    private final ExecutorService executorService;

    /**
     * Callback para listado de registros.
     */
    public interface ListaCallback {
        void onLista(List<Registro> registros);

        void onError(String mensaje);
    }

    /**
     * Callback para eliminacion.
     */
    public interface EliminarCallback {
        void onEliminado();

        void onError(String mensaje);
    }

    /**
     * Callback para conteo.
     */
    public interface ConteoCallback {
        void onConteo(int totalGastos, int totalIngresos);
    }

    /**
     * Constructor con inyeccion de dependencias.
     */
    public HistorialController(Activity activity, RegistroRepository registroRepository) {
        this.activity = activity;
        this.registroRepository = registroRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Carga registros con filtros aplicados.
     *
     * @param filtroTipo Tipo de filtro: "TODOS", "GASTO", "INGRESO"
     * @param parcelaId  ID de parcela (0 para todas)
     * @param anio       Año ("TODOS" para todos)
     * @param mes        Mes ("TODOS" para todos)
     * @param callback   Callback con lista de registros
     */
    public void cargarRegistros(String filtroTipo, int parcelaId, String anio, String mes, ListaCallback callback) {
        executorService.execute(() -> {
            List<Registro> registros;

            boolean tieneAnio = !"TODOS".equals(anio);
            boolean tieneMes = !"TODOS".equals(mes);
            boolean tieneParcela = parcelaId > 0;
            boolean tieneTipo = "GASTO".equals(filtroTipo) || "INGRESO".equals(filtroTipo);

            if (tieneAnio && tieneMes) {
                registros = registroRepository.obtenerPorAnioYMes(anio, mes);
            } else if (tieneAnio) {
                registros = registroRepository.obtenerPorAnio(anio);
            } else {
                registros = registroRepository.obtenerTodos();
            }

            // Filtrar en memoria solo los casos combinados no cubiertos por el DAO
            if (tieneParcela && tieneTipo) {
                final int pid = parcelaId;
                final String tipo = filtroTipo;
                registros.removeIf(r -> r.getParcelaId() != pid || !tipo.equals(r.getTipo()));
            } else if (tieneParcela) {
                final int pid = parcelaId;
                registros.removeIf(r -> r.getParcelaId() != pid);
            } else if (tieneTipo) {
                final String tipo = filtroTipo;
                registros.removeIf(r -> !tipo.equals(r.getTipo()));
            }

            activity.runOnUiThread(() -> callback.onLista(registros));
        });
    }

    /**
     * Carga registros de una fecha específica.
     */
    public void cargarRegistrosPorFecha(String fecha, ListaCallback callback) {
        executorService.execute(() -> {
            List<Registro> registros = registroRepository.obtenerPorFecha(fecha);
            activity.runOnUiThread(() -> callback.onLista(registros));
        });
    }

    /**
     * Obtiene lista de años con registros.
     */
    public void obtenerAniosDisponibles(AniosCallback callback) {
        executorService.execute(() -> {
            List<String> anios = registroRepository.obtenerAniosConRegistros();
            activity.runOnUiThread(() -> callback.onAnios(anios));
        });
    }

    /**
     * Obtiene lista de meses con registros en un año.
     */
    public void obtenerMesesDisponibles(String anio, MesesCallback callback) {
        executorService.execute(() -> {
            List<String> meses = registroRepository.obtenerMesesConRegistros(anio);
            activity.runOnUiThread(() -> callback.onMeses(meses));
        });
    }

    /**
     * Callback para lista de años.
     */
    public interface AniosCallback {
        void onAnios(List<String> anios);

        void onError(String mensaje);
    }

    /**
     * Callback para lista de meses.
     */
    public interface MesesCallback {
        void onMeses(List<String> meses);

        void onError(String mensaje);
    }

    /**
     * Elimina un registro por ID.
     *
     * @param id       Identificador del registro
     * @param callback Callback de resultado
     */
    public void eliminarRegistro(int id, EliminarCallback callback) {
        executorService.execute(() -> {
            registroRepository.eliminarPorId(id);
            activity.runOnUiThread(callback::onEliminado);
        });
    }

    /**
     * Obtiene conteo de registros.
     *
     * @param callback Callback con conteos
     */
    public void obtenerConteos(ConteoCallback callback) {
        executorService.execute(() -> {
            List<Registro> todos = registroRepository.obtenerTodos();
            int gastos = 0;
            int ingresos = 0;

            for (Registro r : todos) {
                if (r.esGasto())
                    gastos++;
                else if (r.esIngreso())
                    ingresos++;
            }

            final int totalGastos = gastos;
            final int totalIngresos = ingresos;

            activity.runOnUiThread(() -> callback.onConteo(totalGastos, totalIngresos));
        });
    }

    /**
     * Formatea fecha para visualizacion.
     *
     * @param fecha Fecha en formato yyyy-MM-dd
     * @return Fecha formateada
     */
    public String formatearFechaVisual(String fecha) {
        if (fecha == null)
            return "";

        try {
            String[] partes = fecha.split("-");
            if (partes.length == 3) {
                String[] meses = { "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };
                int mes = Integer.parseInt(partes[1]);
                return partes[2] + " " + meses[mes];
            }
        } catch (Exception e) {
            // Fallback al formato original
        }
        return fecha;
    }

    /**
     * Formatea monto para visualizacion.
     *
     * @param monto   Monto
     * @param esGasto true si es gasto
     * @return String formateado
     */
    public String formatearMonto(double monto, boolean esGasto) {
        return String.format("S/ %,.2f", monto);
    }
}
