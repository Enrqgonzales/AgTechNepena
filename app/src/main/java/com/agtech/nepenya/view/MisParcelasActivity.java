package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.controller.MisParcelasController;
import com.agtech.nepenya.model.entity.Parcela;
import com.agtech.nepenya.utils.PrefsManager;
import com.agtech.nepenya.view.adapter.ParcelasAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity para gestionar las parcelas del usuario.
 * Permite ver, agregar y eliminar parcelas.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class MisParcelasActivity extends AppCompatActivity implements
        ParcelasAdapter.OnParcelaListener,
        MisParcelasController.ParcelasCallback {

    private static final String[] CULTIVOS = {
            "Seleccionar cultivo", "Palta", "Mango", "Caña de azúcar",
            "Maíz", "Espárrago", "Maracuyá", "Frijol", "Papa", "Otro"
    };

    private MisParcelasController controller;
    private ParcelasAdapter adapter;
    private PrefsManager prefsManager;
    private List<Parcela> parcelasList = new ArrayList<>();

    private LinearLayout layoutVacio;
    private RecyclerView recyclerParcelas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_parcelas);

        prefsManager = new PrefsManager(this);
        controller = new MisParcelasController(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarParcelas();
    }

    private void initViews() {
        layoutVacio = findViewById(R.id.layout_vacio);
        recyclerParcelas = findViewById(R.id.recycler_parcelas);

        adapter = new ParcelasAdapter(parcelasList, this);
        recyclerParcelas.setLayoutManager(new LinearLayoutManager(this));
        recyclerParcelas.setAdapter(adapter);

        initSwipeToDelete();

        FloatingActionButton fabAgregar = findViewById(R.id.fab_agregar);
        fabAgregar.setOnClickListener(v -> mostrarDialogoAgregarParcela());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
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

    private void initSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView rv,
                    @androidx.annotation.NonNull RecyclerView.ViewHolder vh,
                    @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Parcela parcela = adapter.getParcela(position);
                if (parcela != null) {
                    new AlertDialog.Builder(MisParcelasActivity.this)
                            .setTitle(getString(R.string.eliminar))
                            .setMessage(getString(R.string.confirmar_eliminar_parcela))
                            .setPositiveButton(getString(R.string.eliminar), (d, w) -> {
                                controller.eliminarParcela(parcela, new MisParcelasController.EliminarCallback() {
                                    @Override
                                    public void onEliminada() {
                                        adapter.eliminarItem(position);
                                        Toast.makeText(MisParcelasActivity.this,
                                                getString(R.string.parcela_eliminada), Toast.LENGTH_SHORT).show();
                                        actualizarEstadoVacio();
                                    }

                                    @Override
                                    public void onError(String mensaje) {
                                        adapter.notifyItemChanged(position);
                                        Toast.makeText(MisParcelasActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton(getString(R.string.cancelar),
                                    (d, w) -> adapter.notifyItemChanged(position))
                            .setCancelable(false)
                            .show();
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerParcelas);
    }

    private void cargarParcelas() {
        int usuarioId = prefsManager.getUserId();
        controller.cargarParcelas(usuarioId, this);
    }

    private void mostrarDialogoAgregarParcela() {
        int usuarioId = prefsManager.getUserId();
        controller.mostrarDialogoAgregarParcela(R.layout.dialog_agregar_parcela, CULTIVOS, usuarioId,
                new MisParcelasController.GuardarCallback() {
                    @Override
                    public void onSuccess(long id) {
                        Toast.makeText(MisParcelasActivity.this,
                                getString(R.string.parcela_guardada), Toast.LENGTH_SHORT).show();
                        cargarParcelas();
                    }

                    @Override
                    public void onError(String mensaje) {
                        Toast.makeText(MisParcelasActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarEstadoVacio() {
        if (adapter.getItemCount() == 0) {
            layoutVacio.setVisibility(View.VISIBLE);
            recyclerParcelas.setVisibility(View.GONE);
        } else {
            layoutVacio.setVisibility(View.GONE);
            recyclerParcelas.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onParcelasCargadas(List<Parcela> parcelas) {
        this.parcelasList = parcelas;
        adapter.actualizarLista(parcelas);
        actualizarEstadoVacio();
    }

    @Override
    public void onError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onParcelaClick(Parcela parcela) {
        Toast.makeText(this, parcela.getNombre() + " - " + parcela.getCultivo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onParcelaEliminar(Parcela parcela, int position) {
        // manejado por swipe
    }
}
