package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.controller.ReportesController;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.repository.RegistroRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity de Reportes de Rentabilidad.
 * Muestra metricas financieras y permite exportar.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class ReportesActivity extends AppCompatActivity implements
        ReportesController.ReporteCallback,
        ReportesController.ExportarCallback {

    private ReportesController controller;
    private int anioActual;

    // UI Elements
    private ImageButton btnAnterior;
    private ImageButton btnSiguiente;
    private TextView tvCampania;
    private TextView tvRentabilidad;
    private TextView tvTotalGastos;
    private TextView tvTotalIngresos;
    private ProgressBar progressGastos;
    private ProgressBar progressIngresos;
    private TableLayout tableCategorias;
    private Button btnCompartir;

    private List<Registro> registrosActuales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        initController();
        initViews();
        initListeners();

        anioActual = controller.obtenerAnioActual();
        cargarReporte();
    }

    private void initController() {
        AppDatabase db = AppDatabase.getInstance(this);
        RegistroRepository registroRepo = new RegistroRepository(db.registroDao());
        controller = new ReportesController(this, registroRepo);
    }

    private void initViews() {
        btnAnterior = findViewById(R.id.btn_anterior);
        btnSiguiente = findViewById(R.id.btn_siguiente);
        tvCampania = findViewById(R.id.tv_campania);
        tvRentabilidad = findViewById(R.id.tv_rentabilidad);
        tvTotalGastos = findViewById(R.id.tv_total_gastos);
        tvTotalIngresos = findViewById(R.id.tv_total_ingresos);
        progressGastos = findViewById(R.id.progress_gastos);
        progressIngresos = findViewById(R.id.progress_ingresos);
        tableCategorias = findViewById(R.id.table_categorias);
        btnCompartir = findViewById(R.id.btn_compartir);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_reportes);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reportes)
                return true;
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_registro) {
                startActivity(new Intent(this, RegistroActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_ajustes) {
                startActivity(new Intent(this, AccesibilidadActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void initListeners() {
        btnAnterior.setOnClickListener(v -> {
            anioActual--;
            cargarReporte();
        });

        btnSiguiente.setOnClickListener(v -> {
            anioActual++;
            cargarReporte();
        });

        btnCompartir.setOnClickListener(v -> compartirReporte());

    }

    private void cargarReporte() {
        String textoCampania = getString(R.string.campana) + " " + anioActual;
        tvCampania.setText(textoCampania);
        controller.cargarReporte(anioActual, this);
    }

    private void compartirReporte() {
        if (registrosActuales == null || registrosActuales.isEmpty()) {
            Toast.makeText(this, "No hay datos para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        controller.exportarCSV(registrosActuales, this);
    }

    // Callbacks del Controller

    @Override
    public void onReporte(ReportesController.ReporteModel reporte) {
        // Actualizar rentabilidad
        tvRentabilidad.setText(controller.formatearRentabilidad(reporte.rentabilidadNeta));

        int colorRentabilidad = reporte.rentabilidadNeta >= 0
                ? getColor(R.color.accent_green)
                : getColor(R.color.error_red);
        tvRentabilidad.setTextColor(colorRentabilidad);

        // Totales
        tvTotalGastos.setText(controller.formatearMonto(reporte.totalGastos));
        tvTotalGastos.setTextColor(getColor(R.color.error_red));

        tvTotalIngresos.setText(controller.formatearMonto(reporte.totalIngresos));
        tvTotalIngresos.setTextColor(getColor(R.color.accent_green));

        // Progress bars
        double total = reporte.totalGastos + reporte.totalIngresos;
        if (total > 0) {
            progressGastos.setProgress(controller.calcularPorcentaje(reporte.totalGastos, total));
            progressIngresos.setProgress(controller.calcularPorcentaje(reporte.totalIngresos, total));
        } else {
            progressGastos.setProgress(0);
            progressIngresos.setProgress(0);
        }

        // Tabla de categorias
        tableCategorias.removeAllViews();

        // Header
        TableRow headerRow = new TableRow(this);
        headerRow.setPadding(0, 16, 0, 16);

        TextView tvHeaderCat = new TextView(this);
        tvHeaderCat.setText("CATEGORÍA");
        tvHeaderCat.setTextSize(12);
        tvHeaderCat.setTextColor(getColor(R.color.secondary_text));
        tvHeaderCat.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
        headerRow.addView(tvHeaderCat);

        TextView tvHeaderGasto = new TextView(this);
        tvHeaderGasto.setText("GASTOS");
        tvHeaderGasto.setTextSize(12);
        tvHeaderGasto.setTextColor(getColor(R.color.secondary_text));
        tvHeaderGasto.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvHeaderGasto.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(tvHeaderGasto);

        TextView tvHeaderIngreso = new TextView(this);
        tvHeaderIngreso.setText("INGRESOS");
        tvHeaderIngreso.setTextSize(12);
        tvHeaderIngreso.setTextColor(getColor(R.color.secondary_text));
        tvHeaderIngreso.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvHeaderIngreso.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(tvHeaderIngreso);

        tableCategorias.addView(headerRow);

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(getColor(R.color.divider));
        tableCategorias.addView(divider);

        // Data rows
        for (ReportesController.CategoriaItem item : reporte.items) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 16, 0, 16);

            TextView tvCat = new TextView(this);
            tvCat.setText(item.categoria);
            tvCat.setTextSize(14);
            tvCat.setTextColor(getColor(R.color.primary_text));
            tvCat.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            row.addView(tvCat);

            TextView tvGasto = new TextView(this);
            if (item.gastos > 0) {
                tvGasto.setText(String.format(Locale.getDefault(), "S/ %,.2f", item.gastos));
                tvGasto.setTextColor(getColor(R.color.error_red));
            } else {
                tvGasto.setText("-");
                tvGasto.setTextColor(getColor(R.color.secondary_text));
            }
            tvGasto.setTextSize(14);
            tvGasto.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvGasto.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tvGasto);

            TextView tvIngreso = new TextView(this);
            if (item.ingresos > 0) {
                tvIngreso.setText(String.format(Locale.getDefault(), "S/ %,.2f", item.ingresos));
                tvIngreso.setTextColor(getColor(R.color.accent_green));
            } else {
                tvIngreso.setText("-");
                tvIngreso.setTextColor(getColor(R.color.secondary_text));
            }
            tvIngreso.setTextSize(14);
            tvIngreso.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvIngreso.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tvIngreso);

            tableCategorias.addView(row);
        }

        // Guardar registros para exportacion
        String anioStr = String.valueOf(anioActual);
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            RegistroRepository repo = new RegistroRepository(db.registroDao());
            registrosActuales = repo.obtenerPorAnio(anioStr);
        }).start();
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExportado(String contenido) {
        try {
            // Guardar en archivo temporal
            File file = new File(getCacheDir(), "reporte_agtech_" + anioActual + ".csv");
            FileWriter writer = new FileWriter(file);
            writer.write(contenido);
            writer.close();

            // Compartir
            Intent intent = controller.crearIntentCompartir(
                    contenido, "Reporte AgTech Nepeña " + anioActual);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Error al exportar: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
