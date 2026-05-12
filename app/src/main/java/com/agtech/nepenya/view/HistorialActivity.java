package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.controller.HistorialController;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.model.entity.Registro;
import com.agtech.nepenya.model.repository.ParcelaRepository;
import com.agtech.nepenya.model.repository.RegistroRepository;
import com.agtech.nepenya.view.adapter.RegistroAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import java.util.Locale;

/**
 * Activity de Historial de registros.
 * Muestra lista con filtros y swipe-to-delete.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class HistorialActivity extends AppCompatActivity implements
        HistorialController.ListaCallback,
        HistorialController.EliminarCallback,
        RegistroAdapter.OnRegistroClickListener {

    private HistorialController controller;
    private RegistroAdapter adapter;

    // UI Elements
    private TabLayout tabLayout;
    private Spinner spinnerParcela;
    private Spinner spinnerAnio;
    private Spinner spinnerMes;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private List<Parcela> parcelasList;
    private String filtroTipo = "TODOS";
    private int filtroParcelaId = 0;
    private String filtroAnio = "TODOS";
    private String filtroMes = "TODOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        initController();
        initViews();
        initListeners();
        initRecyclerView();

        // Cargar parcelas para spinner
        cargarParcelas();

        // Cargar registros
        cargarRegistros();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarRegistros();
    }

    private void initController() {
        AppDatabase db = AppDatabase.getInstance(this);
        RegistroRepository registroRepo = new RegistroRepository(db.registroDao());
        controller = new HistorialController(this, registroRepo);
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        spinnerParcela = findViewById(R.id.spinner_parcela);
        spinnerAnio = findViewById(R.id.spinner_anio);
        spinnerMes = findViewById(R.id.spinner_mes);
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        // Configurar spinners de fecha con opciones "Todos"
        String[] anios = { "Todos los años", "2024", "2025", "2026", "2027", "2028" };
        String[] meses = { "Todos los meses", "01 - Enero", "02 - Febrero", "03 - Marzo", "04 - Abril",
                "05 - Mayo", "06 - Junio", "07 - Julio", "08 - Agosto", "09 - Septiembre",
                "10 - Octubre", "11 - Noviembre", "12 - Diciembre" };

        ArrayAdapter<String> anioAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, anios);
        anioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnio.setAdapter(anioAdapter);

        ArrayAdapter<String> mesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, meses);
        mesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMes.setAdapter(mesAdapter);

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.todos)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.gastos)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.ingresos)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_historial);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historial)
                return true;
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_registro) {
                startActivity(new Intent(this, RegistroActivity.class));
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
        // Tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    filtroTipo = "TODOS";
                } else if (position == 1) {
                    filtroTipo = "GASTO";
                } else {
                    filtroTipo = "INGRESO";
                }
                cargarRegistros();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Spinner parcela
        spinnerParcela.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parcelasList != null) {
                    if (position == 0) {
                        filtroParcelaId = 0; // Todas
                    } else if (position - 1 < parcelasList.size()) {
                        filtroParcelaId = parcelasList.get(position - 1).getId();
                    }
                }
                cargarRegistros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Spinner año
        spinnerAnio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    filtroAnio = "TODOS";
                } else {
                    filtroAnio = (String) parent.getItemAtPosition(position);
                }
                cargarRegistros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Spinner mes
        spinnerMes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    filtroMes = "TODOS";
                } else {
                    String mesCompleto = (String) parent.getItemAtPosition(position);
                    // Extraer solo el número de mes (formato: "01 - Enero")
                    filtroMes = mesCompleto.split(" - ")[0];
                }
                cargarRegistros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void initRecyclerView() {
        adapter = new RegistroAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Registro registro = adapter.getRegistro(position);

                if (registro != null) {
                    new AlertDialog.Builder(HistorialActivity.this)
                            .setTitle("Eliminar registro")
                            .setMessage("¿Esta seguro de eliminar este registro?")
                            .setPositiveButton("Eliminar",
                                    (dialog, which) -> controller.eliminarRegistro(registro.getId(),
                                            new HistorialController.EliminarCallback() {
                                                @Override
                                                public void onEliminado() {
                                                    adapter.eliminarRegistro(position);
                                                    Toast.makeText(HistorialActivity.this,
                                                            "Registro eliminado", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onError(String mensaje) {
                                                    Toast.makeText(HistorialActivity.this,
                                                            mensaje, Toast.LENGTH_SHORT).show();
                                                    adapter.notifyItemChanged(position);
                                                }
                                            }))
                            .setNegativeButton("Cancelar", (dialog, which) -> adapter.notifyItemChanged(position))
                            .setCancelable(false)
                            .show();
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void cargarParcelas() {
        AppDatabase db = AppDatabase.getInstance(this);
        ParcelaRepository parcelaRepo = new ParcelaRepository(db.parcelaDao());

        new Thread(() -> {
            parcelasList = parcelaRepo.obtenerTodas();
            List<String> nombres = new ArrayList<>();
            nombres.add("Todas las parcelas");
            for (Parcela p : parcelasList) {
                nombres.add(p.getNombre());
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, nombres);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerParcela.setAdapter(adapter);
            });
        }).start();
    }

    private void cargarRegistros() {
        controller.cargarRegistros(filtroTipo, filtroParcelaId, filtroAnio, filtroMes, this);
    }

    // Callbacks del Controller

    @Override
    public void onLista(List<Registro> registros) {
        adapter.actualizarLista(registros);

        if (registros.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEliminado() {
        cargarRegistros();
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    // Callbacks del Adapter

    @Override
    public void onRegistroClick(Registro registro) {
        // Mostrar detalles del registro
        String mensaje = String.format(Locale.getDefault(), "%s\n%s\nS/ %.2f\n%s",
                registro.getCategoria(),
                registro.getFecha(),
                registro.getMonto(),
                registro.getDescripcion());

        new AlertDialog.Builder(this)
                .setTitle(registro.getTipo())
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    @Override
    public void onRegistroEliminar(Registro registro) {
        // Manejado en swipe
    }
}
