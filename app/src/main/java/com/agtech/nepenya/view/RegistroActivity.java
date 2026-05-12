package com.agtech.nepenya.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.accessibility.VoiceCommandManager;
import com.agtech.nepenya.controller.RegistroController;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.model.repository.RegistroRepository;
import com.agtech.nepenya.utils.NetworkUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity para crear nuevos registros de gasto o ingreso.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class RegistroActivity extends AppCompatActivity implements
        RegistroController.ParcelasCallback,
        RegistroController.GuardarCallback,
        RegistroController.ValidacionCallback {

    private RegistroController controller;
    private VoiceCommandManager voiceCommandManager;
    private List<Parcela> parcelasList;

    // UI Elements
    private LinearLayout offlineBanner;
    private LinearLayout layoutSinParcelas;
    private View scrollFormulario;
    private Spinner spinnerParcela;
    private RadioGroup radioGroupTipo;
    private LinearLayout containerCategorias;
    private TextView tvMonto;
    private EditText etDescripcion;
    private ImageButton btnVoice;
    private Button btnGuardar;
    private TextView tvFecha;

    private String selectedFecha;
    private String selectedTipo = "GASTO";
    private int selectedParcelaId = -1;
    private String selectedCategoria = "";
    private double montoActual = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        initController();
        initViews();
        initListeners();

        // Cargar parcelas
        controller.cargarParcelas(this);

        // Verificar estado de conexion
        checkConexion();

        // Establecer fecha actual
        selectedFecha = controller.obtenerFechaActual();
        actualizarTextoFecha();
    }

    private void initController() {
        AppDatabase db = AppDatabase.getInstance(this);
        ParcelaRepository parcelaRepo = new ParcelaRepository(db.parcelaDao());
        RegistroRepository registroRepo = new RegistroRepository(db.registroDao());
        controller = new RegistroController(this, parcelaRepo, registroRepo);
        voiceCommandManager = new VoiceCommandManager();
    }

    private void initViews() {
        offlineBanner = findViewById(R.id.offline_banner);
        layoutSinParcelas = findViewById(R.id.layout_sin_parcelas);
        scrollFormulario = findViewById(R.id.scroll_formulario);
        spinnerParcela = findViewById(R.id.spinner_parcela);
        radioGroupTipo = findViewById(R.id.radio_group_tipo);
        containerCategorias = findViewById(R.id.container_categorias);
        tvMonto = findViewById(R.id.tv_monto);
        etDescripcion = findViewById(R.id.et_descripcion);
        btnVoice = findViewById(R.id.btn_voice);
        btnGuardar = findViewById(R.id.btn_guardar);
        tvFecha = findViewById(R.id.tv_fecha);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_registro);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_registro)
                return true;
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_historial) {
                startActivity(new Intent(this, HistorialActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reportes) {
                startActivity(new Intent(this, ReportesActivity.class));
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
        // Botón ir a parcelas (cuando no hay ninguna)
        Button btnIrParcelas = findViewById(R.id.btn_ir_parcelas);
        if (btnIrParcelas != null) {
            btnIrParcelas.setOnClickListener(v -> {
                startActivity(new Intent(this, MisParcelasActivity.class));
                finish();
            });
        }

        // Spinner de parcelas
        spinnerParcela.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parcelasList != null && position < parcelasList.size()) {
                    selectedParcelaId = parcelasList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Radio group tipo
        radioGroupTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_gasto) {
                selectedTipo = "GASTO";
            } else {
                selectedTipo = "INGRESO";
            }
            actualizarCategorias();
        });

        // Boton de voz para descripcion
        btnVoice.setOnClickListener(v -> voiceCommandManager.startListening(this, result -> {
            String currentText = etDescripcion.getText().toString();
            String newText = currentText + " " + result;
            etDescripcion.setText(newText.trim());
        }));

        // Boton guardar
        btnGuardar.setOnClickListener(v -> {
            controller.validarCampos(selectedParcelaId, selectedTipo, selectedCategoria,
                    montoActual, selectedFecha, this);
        });

        // Selector de fecha
        tvFecha.setOnClickListener(v -> mostrarDatePicker());

        // Monto (click para editar)
        tvMonto.setOnClickListener(v -> mostrarDialogMonto());
    }

    private void checkConexion() {
        if (!NetworkUtils.isConnected(this)) {
            offlineBanner.setVisibility(View.VISIBLE);
        } else {
            offlineBanner.setVisibility(View.GONE);
        }
    }

    private void actualizarCategorias() {
        containerCategorias.removeAllViews();
        String[] categorias = controller.obtenerCategorias(selectedTipo);

        LinearLayout row = null;
        for (int i = 0; i < categorias.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                containerCategorias.addView(row);
            }

            Button btnCategoria = new Button(this);
            btnCategoria.setText(categorias[i]);
            btnCategoria.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            btnCategoria.setPadding(8, 16, 8, 16);
            btnCategoria.setTextSize(12);

            final String categoria = categorias[i];
            btnCategoria.setOnClickListener(v -> {
                selectedCategoria = categoria;
                actualizarBotonesCategoria(categorias, categoria);
            });

            if (row != null) {
                row.addView(btnCategoria);
            }
        }

        // Seleccionar primera categoria por defecto
        if (categorias.length > 0) {
            selectedCategoria = categorias[0];
            actualizarBotonesCategoria(categorias, selectedCategoria);
        }
    }

    private void actualizarBotonesCategoria(String[] categorias, String seleccionada) {
        for (int i = 0; i < containerCategorias.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) containerCategorias.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                Button btn = (Button) row.getChildAt(j);
                String cat = btn.getText().toString();
                if (cat.equals(seleccionada)) {
                    btn.setBackgroundResource(R.drawable.bg_chip_active);
                    btn.setTextColor(getColor(R.color.white));
                } else {
                    btn.setBackgroundResource(R.drawable.bg_chip_inactive);
                    btn.setTextColor(getColor(R.color.primary_text));
                }
            }
        }
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedFecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            actualizarTextoFecha();
        }, year, month, day);

        dialog.show();
    }

    private void actualizarTextoFecha() {
        try {
            String[] partes = selectedFecha.split("-");
            String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };
            int mes = Integer.parseInt(partes[1]) - 1;
            tvFecha.setText(partes[2] + " " + meses[mes] + " " + partes[0]);
        } catch (Exception e) {
            tvFecha.setText(selectedFecha);
        }
    }

    private void mostrarDialogMonto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ingrese el monto");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("0.00");
        builder.setView(input);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            try {
                montoActual = Double.parseDouble(input.getText().toString());
                tvMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", montoActual));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Monto invalido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Callbacks del Controller

    @Override
    public void onParcelasCargadas(List<Parcela> parcelas) {
        this.parcelasList = parcelas;

        if (parcelas.isEmpty()) {
            // Mostrar mensaje sin parcelas, ocultar formulario
            layoutSinParcelas.setVisibility(View.VISIBLE);
            scrollFormulario.setVisibility(View.GONE);
            return;
        }

        layoutSinParcelas.setVisibility(View.GONE);
        scrollFormulario.setVisibility(View.VISIBLE);

        List<String> nombres = new ArrayList<>();
        for (Parcela p : parcelas) {
            nombres.add(p.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParcela.setAdapter(adapter);

        if (!parcelas.isEmpty()) {
            selectedParcelaId = parcelas.get(0).getId();
        }

        actualizarCategorias();
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onValido() {
        controller.guardarRegistro(selectedParcelaId, selectedTipo, selectedCategoria,
                montoActual, selectedFecha, etDescripcion.getText().toString(), this);
    }

    @Override
    public void onInvalido(String mensajeError) {
        Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess() {
        Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2001) { // VoiceCommandManager PERMISSION_REQUEST_CODE
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, reiniciar escucha si se presionó el FAB
                if (voiceCommandManager != null) {
                    voiceCommandManager.startListening(this, this::procesarComandoVoz);
                }
            } else {
                Toast.makeText(this, "Permiso de audio denegado. Comandos de voz no disponibles.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Procesa comandos de voz recibidos.
     */
    private void procesarComandoVoz(String comando) {
        comando = comando.toLowerCase();
        if (comando.contains("guardar") || comando.contains("salvar")) {
            // Usar el mismo flujo de validación/guardado que el botón
            controller.validarCampos(selectedParcelaId, selectedTipo, selectedCategoria,
                    montoActual, selectedFecha, this);
        } else if (comando.contains("cancelar") || comando.contains("atras")) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceCommandManager != null) {
            voiceCommandManager.destroy();
        }
    }
}
