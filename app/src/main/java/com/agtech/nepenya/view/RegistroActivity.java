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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import com.agtech.nepenya.model.repository.InventarioRepository;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.model.repository.RegistroRepository;
import com.agtech.nepenya.utils.NetworkUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
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
    private android.widget.ScrollView scrollFormulario;
    private Spinner spinnerParcela;
    private RadioGroup radioGroupTipo;
    private ChipGroup chipGroupCategorias;
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

    // New UI elements for physical inputs
    private LinearLayout layoutCantidadSection;
    private EditText etCantidad;
    private Spinner spinnerUnidad;
    private EditText etCostoUnitario;
    private TextView tvMontoCalculado;

    // Physical input categories (show quantity/unit/costo fields)
    private static final List<String> CATEGORIAS_FISICAS = Arrays.asList(
            "Fertilizantes", "Pesticidas", "Combustible", "Siembra", "Riego", "Transporte");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessibilityPrefs.applyAll(this);
        setContentView(R.layout.activity_registro);

        initController();
        initViews();
        initListeners();

        // Cargar/preseleccionar tipo de registro si viene como extra en el Intent
        if (getIntent() != null && getIntent().hasExtra("TIPO")) {
            String tipoExtra = getIntent().getStringExtra("TIPO");
            if ("GASTO".equals(tipoExtra)) {
                radioGroupTipo.check(R.id.radio_gasto);
                selectedTipo = "GASTO";
            } else if ("INGRESO".equals(tipoExtra)) {
                radioGroupTipo.check(R.id.radio_ingreso);
                selectedTipo = "INGRESO";
            }
        }

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
        InventarioRepository inventarioRepo = new InventarioRepository(db.inventarioDao());
        controller = new RegistroController(this, parcelaRepo, registroRepo, inventarioRepo);
        voiceCommandManager = new VoiceCommandManager();
    }

    private void initViews() {
        offlineBanner = findViewById(R.id.offline_banner);
        layoutSinParcelas = findViewById(R.id.layout_sin_parcelas);
        scrollFormulario = findViewById(R.id.scroll_formulario);
        spinnerParcela = findViewById(R.id.spinner_parcela);
        radioGroupTipo = findViewById(R.id.radio_group_tipo);
        chipGroupCategorias = findViewById(R.id.chip_group_categorias);
        tvMonto = findViewById(R.id.tv_monto);
        etDescripcion = findViewById(R.id.et_descripcion);
        btnVoice = findViewById(R.id.btn_voice);
        btnGuardar = findViewById(R.id.btn_guardar);
        tvFecha = findViewById(R.id.tv_fecha);

        // Initialize new quantity fields
        layoutCantidadSection = findViewById(R.id.layout_cantidad_section);
        etCantidad = findViewById(R.id.et_cantidad);
        spinnerUnidad = findViewById(R.id.spinner_unidad);
        etCostoUnitario = findViewById(R.id.et_costo_unitario);
        tvMontoCalculado = findViewById(R.id.tv_monto_calculado);

        initUnidadesSpinner();

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
            actualizarCantidadSection();
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

        // TextWatchers for auto-calculating monto from cantidad * costo_unitario
        etCantidad.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularMonto();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        etCostoUnitario.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularMonto();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void initUnidadesSpinner() {
        List<String> unidades = new ArrayList<>();
        unidades.add("kg");
        unidades.add("litros");
        unidades.add("sacos");
        unidades.add("paquetes");
        unidades.add("galones");
        unidades.add("unidades");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, unidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidad.setAdapter(adapter);
    }

    private void actualizarCantidadSection() {
        if ("INGRESO".equals(selectedTipo)) {
            // INGRESO: hide quantity section entirely
            layoutCantidadSection.setVisibility(View.GONE);
            tvMonto.setVisibility(View.VISIBLE);
            return;
        }

        // GASTO: check if physical input category
        boolean esFisica = CATEGORIAS_FISICAS.contains(selectedCategoria);
        if (esFisica) {
            layoutCantidadSection.setVisibility(View.VISIBLE);
            tvMonto.setVisibility(View.GONE); // Use calculated monto instead
        } else {
            layoutCantidadSection.setVisibility(View.GONE);
            tvMonto.setVisibility(View.VISIBLE);
        }
    }

    private void calcularMonto() {
        try {
            double cantidad = Double.parseDouble(etCantidad.getText().toString().replace(",", "."));
            double costoUnitario = Double.parseDouble(etCostoUnitario.getText().toString().replace(",", "."));
            montoActual = cantidad * costoUnitario;
            tvMontoCalculado.setText(String.format(Locale.getDefault(), "S/ %.2f", montoActual));
        } catch (NumberFormatException e) {
            montoActual = 0;
            tvMontoCalculado.setText("S/ 0.00");
        }
    }

    public double getCantidad() {
        try {
            return Double.parseDouble(etCantidad.getText().toString().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getUnidad() {
        return spinnerUnidad.getSelectedItem() != null ? spinnerUnidad.getSelectedItem().toString() : "unidades";
    }

    public double getCostoUnitario() {
        try {
            return Double.parseDouble(etCostoUnitario.getText().toString().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isCategoriaFisica() {
        return CATEGORIAS_FISICAS.contains(selectedCategoria);
    }

    private void checkConexion() {
        if (!NetworkUtils.isConnected(this)) {
            offlineBanner.setVisibility(View.VISIBLE);
        } else {
            offlineBanner.setVisibility(View.GONE);
        }
    }

    private void actualizarCategorias() {
        chipGroupCategorias.removeAllViews();
        String[] categorias = controller.obtenerCategorias(selectedTipo);

        for (int i = 0; i < categorias.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(categorias[i]);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_state_color);
            chip.setTextColor(getResources().getColorStateList(
                    R.color.chip_text_state_color, getTheme()));

            final String categoria = categorias[i];
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategoria = categoria;
                    actualizarCantidadSection();
                }
            });

            chipGroupCategorias.addView(chip);

            if (i == 0) {
                chip.setChecked(true);
                selectedCategoria = categorias[0];
            }
        }
        actualizarCantidadSection();
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedFecha = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
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
        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), mensaje, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onValido() {
        btnGuardar.setEnabled(false);
        btnGuardar.setText("Guardando...");
        
        Double cantidad = isCategoriaFisica() ? getCantidad() : null;
        String unidad = isCategoriaFisica() ? getUnidad() : null;
        Double costoUnitario = isCategoriaFisica() ? getCostoUnitario() : null;

        controller.guardarRegistro(selectedParcelaId, selectedTipo, selectedCategoria,
                montoActual, selectedFecha, etDescripcion.getText().toString(),
                cantidad, unidad, costoUnitario, this);
    }

    @Override
    public void onInvalido(String mensajeError) {
        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), mensajeError, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess() {
        com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Registro guardado exitosamente", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
        
        // Reset state
        btnGuardar.setEnabled(true);
        btnGuardar.setText(R.string.guardar_registro);
        etDescripcion.setText("");
        etCantidad.setText("");
        etCostoUnitario.setText("");
        montoActual = 0.0;
        tvMonto.setText("S/ 0.00");
        tvMontoCalculado.setText("S/ 0.00");
        
        // Ensure scroll to top to see the Snackbar and empty fields
        scrollFormulario.smoothScrollTo(0, 0);
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
