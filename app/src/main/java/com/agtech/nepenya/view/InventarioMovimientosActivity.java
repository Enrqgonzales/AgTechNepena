package com.agtech.nepenya.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agtech.nepenya.R;
import com.agtech.nepenya.accessibility.AccessibilityPrefs;
import com.agtech.nepenya.model.database.AppDatabase;
import com.agtech.nepenya.model.entity.InventarioMovimiento;
import com.agtech.nepenya.model.repository.InventarioRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity para ver el historial de movimientos de un item de inventario.
 * Muestra entradas y salidas con fecha y cantidad.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class InventarioMovimientosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTitulo;
    private TextView tvEmpty;
    private MovimientosAdapter adapter;

    private int itemId;
    private String itemNombre;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessibilityPrefs.applyAll(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_movimientos);

        executorService = Executors.newSingleThreadExecutor();

        // Obtener datos del intent
        itemId = getIntent().getIntExtra("item_id", -1);
        itemNombre = getIntent().getStringExtra("item_nombre");

        if (itemId == -1 || itemNombre == null) {
            Toast.makeText(this, "Error: Item no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initRecyclerView();
        cargarMovimientos();
    }

    private void initViews() {
        tvTitulo = findViewById(R.id.tv_titulo);
        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        tvTitulo.setText("Movimientos: " + itemNombre);

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

    private void initRecyclerView() {
        adapter = new MovimientosAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void cargarMovimientos() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(this);
                InventarioRepository repo = new InventarioRepository(db.inventarioDao());
                List<InventarioMovimiento> movimientos = repo.obtenerMovimientosPorItem(itemId);

                runOnUiThread(() -> {
                    if (movimientos.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.actualizarLista(movimientos);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al cargar movimientos: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    /**
     * Adapter interno para mostrar movimientos
     */
    private static class MovimientosAdapter extends RecyclerView.Adapter<MovimientosAdapter.ViewHolder> {

        private List<InventarioMovimiento> movimientos;

        public MovimientosAdapter(List<InventarioMovimiento> movimientos) {
            this.movimientos = movimientos;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_movimiento, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            InventarioMovimiento mov = movimientos.get(position);

            // Tipo (Entrada/Salida)
            holder.tvTipo.setText(mov.getTipo());
            if ("ENTRADA".equals(mov.getTipo())) {
                holder.tvTipo.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
            } else {
                holder.tvTipo.setTextColor(holder.itemView.getContext().getColor(R.color.error_red));
            }

            // Fecha
            holder.tvFecha.setText(mov.getFecha());

            // Cantidad
            holder.tvCantidad.setText(String.format(Locale.getDefault(), "%.2f %s", 
                    mov.getCantidad(), mov.getUnidad()));

            // Costo total si aplica
            if (mov.getCostoTotal() > 0) {
                holder.tvCosto.setText(String.format(Locale.getDefault(), "S/ %.2f", 
                        mov.getCostoTotal()));
                holder.tvCosto.setVisibility(View.VISIBLE);
            } else {
                holder.tvCosto.setVisibility(View.GONE);
            }

            // Descripción
            if (mov.getDescripcion() != null && !mov.getDescripcion().isEmpty()) {
                holder.tvDescripcion.setText(mov.getDescripcion());
                holder.tvDescripcion.setVisibility(View.VISIBLE);
            } else {
                holder.tvDescripcion.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return movimientos.size();
        }

        public void actualizarLista(List<InventarioMovimiento> nuevos) {
            this.movimientos = nuevos;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTipo, tvFecha, tvCantidad, tvCosto, tvDescripcion;

            ViewHolder(View view) {
                super(view);
                tvTipo = view.findViewById(R.id.tv_tipo);
                tvFecha = view.findViewById(R.id.tv_fecha);
                tvCantidad = view.findViewById(R.id.tv_cantidad);
                tvCosto = view.findViewById(R.id.tv_costo);
                tvDescripcion = view.findViewById(R.id.tv_descripcion);
            }
        }
    }
}
