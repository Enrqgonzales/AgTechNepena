package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.controller.InventarioController;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.repository.InventarioRepository;
import com.agtech.nepenya.model.repository.ParcelaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity para crear nuevos items en el inventario.
 * Permite ingresar nombre, categoría, cantidad, unidad y costo unitario.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class InventarioFormActivity extends AppCompatActivity implements
        InventarioController.InventarioCallback {

    private InventarioController controller;
    private ParcelaRepository parcelaRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Spinner spinnerParcela;
    private EditText etNombre;
    private Spinner spinnerCategoria;
    private EditText etCantidad;
    private Spinner spinnerUnidad;
    private EditText etCostoUnitario;
    private EditText etDescripcion;
    private Button btnGuardar;
    private Button btnCancelar;

    private List<Parcela> parcelasList = new ArrayList<>();
    private int selectedParcelaId = -1;

    private static final String[] CATEGORIAS = {
            "Seleccionar categoría", "PESTICIDA", "FERTILIZANTE", "SEMILLA", "OTRO"
    };

    private static final String[] UNIDADES = {
            "Seleccionar unidad", "KG", "LITROS", "GALONES", "ML", "PAQUETES", "SACOS"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_form);

        initController();
        initViews();
        initListeners();
        cargarParcelas();
    }

    private void initController() {
        AppDatabase db = AppDatabase.getInstance(this);
        InventarioRepository repo = new InventarioRepository(db.inventarioDao());
        parcelaRepository = new ParcelaRepository(db.parcelaDao());
        controller = new InventarioController(this, repo, db.registroDao());
    }

    private void initViews() {
        spinnerParcela = findViewById(R.id.spinner_parcela);
        etNombre = findViewById(R.id.et_nombre);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        etCantidad = findViewById(R.id.et_cantidad);
        spinnerUnidad = findViewById(R.id.spinner_unidad);
        etCostoUnitario = findViewById(R.id.et_costo_unitario);
        etDescripcion = findViewById(R.id.et_descripcion);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnCancelar = findViewById(R.id.btn_cancelar);

        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIAS);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        ArrayAdapter<String> unidadAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, UNIDADES);
        unidadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidad.setAdapter(unidadAdapter);
    }

    private void cargarParcelas() {
        executorService.execute(() -> {
            List<Parcela> parcelas = parcelaRepository.obtenerTodas();
            runOnUiThread(() -> {
                parcelasList = parcelas;
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
            });
        });
    }

    private void initListeners() {
        btnGuardar.setOnClickListener(v -> guardarItem());
        btnCancelar.setOnClickListener(v -> finish());

        spinnerParcela.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (parcelasList != null && position < parcelasList.size()) {
                    selectedParcelaId = parcelasList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void guardarItem() {
        if (selectedParcelaId == -1) {
            Toast.makeText(this, "Seleccione una parcela", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        int categoriaPos = spinnerCategoria.getSelectedItemPosition();
        String cantidadStr = etCantidad.getText() != null ? etCantidad.getText().toString().trim() : "";
        int unidadPos = spinnerUnidad.getSelectedItemPosition();
        String costoStr = etCostoUnitario.getText() != null ? etCostoUnitario.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del item", Toast.LENGTH_SHORT).show();
            etNombre.requestFocus();
            return;
        }

        if (categoriaPos == 0) {
            Toast.makeText(this, "Seleccione una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingrese la cantidad", Toast.LENGTH_SHORT).show();
            etCantidad.requestFocus();
            return;
        }

        if (unidadPos == 0) {
            Toast.makeText(this, "Seleccione una unidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (costoStr.isEmpty()) {
            Toast.makeText(this, "Ingrese el costo unitario", Toast.LENGTH_SHORT).show();
            etCostoUnitario.requestFocus();
            return;
        }

        double cantidad;
        double costoUnitario;
        try {
            cantidad = Double.parseDouble(cantidadStr);
            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            costoUnitario = Double.parseDouble(costoStr);
            if (costoUnitario < 0) {
                Toast.makeText(this, "El costo no puede ser negativo", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Costo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoria = CATEGORIAS[categoriaPos];
        String unidad = UNIDADES[unidadPos];

        btnGuardar.setEnabled(false);

        controller.crearItem(nombre, categoria, cantidad, unidad, costoUnitario, descripcion,
                selectedParcelaId, this);
    }

    @Override
    public void onInventarioCargado(java.util.List<com.agtech.nepenya.model.entity.InventarioItem> items,
            double valorTotal, int totalItems) {
        // No usado en este contexto
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        btnGuardar.setEnabled(true);
    }

    @Override
    public void onOperacionExitosa(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
