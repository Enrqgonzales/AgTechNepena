package com.agtech.nepenya.controller;

import android.app.Activity;
import android.content.Intent;

import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.repository.RegistroRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller para la pantalla de Reportes.
 * Calcula rentabilidad y genera reportes.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class ReportesController {

    private final Activity activity;
    private final RegistroRepository registroRepository;
    private final ExecutorService executorService;

    /**
     * Callback para reporte cargado.
     */
    public interface ReporteCallback {
        void onReporte(ReporteModel reporte);
        void onError(String mensaje);
    }

    /**
     * Callback para exportacion.
     */
    public interface ExportarCallback {
        void onExportado(String contenido);
        void onError(String mensaje);
    }

    /**
     * Modelo de datos del reporte.
     */
    public static class ReporteModel {
        public final int anio;
        public final double totalGastos;
        public final double totalIngresos;
        public final double rentabilidadNeta;
        public final List<CategoriaItem> items;

        public ReporteModel(int anio, double totalGastos, double totalIngresos,
                           List<CategoriaItem> items) {
            this.anio = anio;
            this.totalGastos = totalGastos;
            this.totalIngresos = totalIngresos;
            this.rentabilidadNeta = totalIngresos - totalGastos;
            this.items = items;
        }
    }

    /**
     * Item de categoria para tabla.
     */
    public static class CategoriaItem {
        public final String categoria;
        public final double gastos;
        public final double ingresos;

        public CategoriaItem(String categoria, double gastos, double ingresos) {
            this.categoria = categoria;
            this.gastos = gastos;
            this.ingresos = ingresos;
        }
    }

    /**
     * Constructor con inyeccion de dependencias.
     */
    public ReportesController(Activity activity, RegistroRepository registroRepository) {
        this.activity = activity;
        this.registroRepository = registroRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Carga reporte para un año especifico.
     *
     * @param anio     Año a consultar
     * @param callback Callback con datos del reporte
     */
    public void cargarReporte(int anio, ReporteCallback callback) {
        executorService.execute(() -> {
            String anioStr = String.valueOf(anio);

            double totalGastos = registroRepository.obtenerTotalGastosPorAnio(anioStr);
            double totalIngresos = registroRepository.obtenerTotalIngresosPorAnio(anioStr);

            // Obtener categorias
            List<String> catGastos = registroRepository.obtenerCategoriasGasto();
            List<String> catIngresos = registroRepository.obtenerCategoriasIngreso();

            List<CategoriaItem> items = new ArrayList<>();

            // Procesar categorias de gastos
            for (String cat : catGastos) {
                double gasto = registroRepository.obtenerGastosPorCategoriaYAnio(anioStr, cat);
                if (gasto > 0) {
                    items.add(new CategoriaItem(cat, gasto, 0));
                }
            }

            // Procesar categorias de ingresos
            for (String cat : catIngresos) {
                double ingreso = registroRepository.obtenerIngresosPorCategoriaYAnio(anioStr, cat);
                if (ingreso > 0) {
                    // Buscar si ya existe categoria
                    boolean encontrado = false;
                    for (CategoriaItem item : items) {
                        if (item.categoria.equals(cat)) {
                            encontrado = true;
                            break;
                        }
                    }
                    if (!encontrado) {
                        items.add(new CategoriaItem(cat, 0, ingreso));
                    }
                }
            }

            ReporteModel reporte = new ReporteModel(anio, totalGastos, totalIngresos, items);
            activity.runOnUiThread(() -> callback.onReporte(reporte));
        });
    }

    /**
     * Exporta registros a CSV.
     *
     * @param registros Lista de registros a exportar
     * @param callback  Callback con contenido CSV
     */
    public void exportarCSV(List<Registro> registros, ExportarCallback callback) {
        executorService.execute(() -> {
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Parcela,Tipo,Categoria,Monto,Descripcion,Fecha,Sincronizado\n");

            for (Registro r : registros) {
                csv.append(r.getId()).append(",");
                csv.append(r.getParcelaId()).append(",");
                csv.append(r.getTipo()).append(",");
                csv.append(escapeCSV(r.getCategoria())).append(",");
                csv.append(r.getMonto()).append(",");
                csv.append(escapeCSV(r.getDescripcion())).append(",");
                csv.append(r.getFecha()).append(",");
                csv.append(r.getSyncStatus()).append("\n");
            }

            activity.runOnUiThread(() -> callback.onExportado(csv.toString()));
        });
    }

    /**
     * Crea intent para compartir contenido.
     *
     * @param contenido Texto a compartir
     * @param asunto    Asunto del mensaje
     * @return Intent configurado
     */
    public Intent crearIntentCompartir(String contenido, String asunto) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, contenido);
        return Intent.createChooser(intent, "Compartir reporte");
    }

    /**
     * Formatea monto para visualizacion.
     *
     * @param monto Monto
     * @return String formateado
     */
    public String formatearMonto(double monto) {
        return String.format(Locale.getDefault(), "S/ %,.2f", monto);
    }

    /**
     * Formatea rentabilidad con signo.
     *
     * @param monto Monto de rentabilidad
     * @return String formateado con signo
     */
    public String formatearRentabilidad(double monto) {
        if (monto >= 0) {
            return String.format(Locale.getDefault(), "+S/ %,.2f", monto);
        } else {
            return String.format(Locale.getDefault(), "-S/ %,.2f", Math.abs(monto));
        }
    }

    /**
     * Obtiene año actual.
     *
     * @return Año actual
     */
    public int obtenerAnioActual() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Calcula porcentaje para barra proporcional.
     *
     * @param valor   Valor parcial
     * @param total   Valor total
     * @return Porcentaje 0-100
     */
    public int calcularPorcentaje(double valor, double total) {
        if (total <= 0) return 0;
        return (int) ((valor / total) * 100);
    }

    /**
     * Escapa valor para CSV.
     */
    private String escapeCSV(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
