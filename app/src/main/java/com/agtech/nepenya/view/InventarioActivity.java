package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.controller.InventarioController;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.InventarioItem;
import com.agtech.nepenya.model.repository.InventarioRepository;
import com.agtech.nepenya.view.adapter.InventarioAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity de Inventario/Almacenamiento.
 * Muestra items de inventario con stock y permite gestionar entradas/salidas.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class InventarioActivity extends AppCompatActivity implements
        InventarioController.InventarioCallback,
        InventarioAdapter.OnInventarioClickListener {

    private InventarioController controller;
    private InventarioAdapter adapter;

    // UI Elements
    private Spinner spinnerCategoria;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvTotalValor;
    private TextView tvTotalItems;
    private FloatingActionButton fabAdd;
    private LinearLayout layoutResumen;

    private String filtroCategoria = "TODAS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        initController();
        initViews();
        initListeners();
        initRecyclerView();
        initCategorias();

        cargarInventario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarInventario();
    }

    private void initController() {
        AppDatabase db = AppDatabase.getInstance(this);
        InventarioRepository repo = new InventarioRepository(db.inventarioDao());
        controller = new InventarioController(this, repo);
    }

    private void initViews() {
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);
        tvTotalValor = findViewById(R.id.tv_total_valor);
        tvTotalItems = findViewById(R.id.tv_total_items);
        fabAdd = findViewById(R.id.fab_add);
        layoutResumen = findViewById(R.id.layout_resumen);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_inicio);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
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
        fabAdd.setOnClickListener(v -> mostrarDialogAgregarItem());

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categorias = { "TODAS", "PESTICIDA", "FERTILIZANTE", "SEMILLA", "OTRO" };
                filtroCategoria = categorias[position];
                cargarInventario();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initRecyclerView() {
        adapter = new InventarioAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initCategorias() {
        List<String> categorias = new ArrayList<>();
        categorias.add("Todas las categorías");
        categorias.add("Pesticidas");
        categorias.add("Fertilizantes");
        categorias.add("Semillas");
        categorias.add("Otros");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
    }

    private void cargarInventario() {
        controller.cargarInventario(filtroCategoria, this);
    }

    private void mostrarDialogAgregarItem() {
        Intent intent = new Intent(this, InventarioFormActivity.class);
        startActivity(intent);
    }

    @Override
    public void onInventarioCargado(List<InventarioItem> items, double valorTotal, int totalItems) {
        adapter.actualizarLista(items);

        if (items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            layoutResumen.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            layoutResumen.setVisibility(View.VISIBLE);

            tvTotalValor.setText(String.format("S/ %.2f", valorTotal));
            tvTotalItems.setText(String.format("%d items", totalItems));
        }
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    // Callbacks del Adapter

    @Override
    public void onItemClick(InventarioItem item) {
        controller.mostrarDialogOpciones(this, item, this);
    }

    @Override
    public void onConsumirClick(InventarioItem item) {
        controller.mostrarDialogConsumir(this, item, this);
    }

    @Override
    public void onAgregarClick(InventarioItem item) {
        controller.mostrarDialogAgregar(this, item, this);
    }

    @Override
    public void onOperacionExitosa(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        cargarInventario();
    }
}
