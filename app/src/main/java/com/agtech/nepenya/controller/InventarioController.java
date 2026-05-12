package com.agtech.nepenya.controller;

import android.app.Activity;

import com.agtech.nepenya.model.entity.InventarioItem;
import com.agtech.nepenya.model.repository.InventarioRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller para la pantalla de Inventario.
 * Gestiona items, movimientos y calculos de stock.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class InventarioController {

    private final Activity activity;
    private final InventarioRepository inventarioRepository;
    private final ExecutorService executorService;

    /**
     * Callback para operaciones de inventario.
     */
    public interface InventarioCallback {
        void onInventarioCargado(List<InventarioItem> items, double valorTotal, int totalItems);

        void onError(String mensaje);

        void onOperacionExitosa(String mensaje);
    }

    /**
     * Constructor con inyeccion de dependencias.
     */
    public InventarioController(Activity activity, InventarioRepository inventarioRepository) {
        this.activity = activity;
        this.inventarioRepository = inventarioRepository;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Carga el inventario con filtro de categoria.
     */
    public void cargarInventario(String categoria, InventarioCallback callback) {
        executorService.execute(() -> {
            try {
                List<InventarioItem> items;

                if ("TODAS".equals(categoria)) {
                    items = inventarioRepository.obtenerTodos();
                } else {
                    items = inventarioRepository.obtenerPorCategoria(categoria);
                }

                double valorTotal = inventarioRepository.obtenerValorTotal();
                int totalItems = inventarioRepository.contarItemsConStock();

                final double finalValorTotal = valorTotal;
                final int finalTotalItems = totalItems;

                activity.runOnUiThread(() -> callback.onInventarioCargado(items, finalValorTotal, finalTotalItems));
            } catch (Exception e) {
                activity.runOnUiThread(() -> callback.onError("Error al cargar inventario: " + e.getMessage()));
            }
        });
    }

    /**
     * Consume stock de un item.
     */
    public void consumirItem(int itemId, double cantidad, InventarioCallback callback) {
        executorService.execute(() -> {
            try {
                InventarioItem item = inventarioRepository.obtenerItem(itemId);

                if (item == null) {
                    activity.runOnUiThread(() -> callback.onError("Item no encontrado"));
                    return;
                }

                if (item.getCantidad() < cantidad) {
                    activity.runOnUiThread(() -> callback.onError("Stock insuficiente. Disponible: " +
                            String.format(Locale.getDefault(), "%.2f %s", item.getCantidad(), item.getUnidad())));
                    return;
                }

                double costoTotal = cantidad * item.getCostoUnitario();
                String fecha = obtenerFechaActual();
                String descripcion = "Consumo: " + cantidad + " " + item.getUnidad() + " de " + item.getNombre();

                // Consumir del inventario
                boolean exito = inventarioRepository.consumirStock(itemId, cantidad, costoTotal,
                        fecha, descripcion, null);

                if (exito) {
                    activity.runOnUiThread(() -> callback.onOperacionExitosa(String.format(Locale.getDefault(),
                            "Consumido %.2f %s de %s", cantidad, item.getUnidad(), item.getNombre())));
                } else {
                    activity.runOnUiThread(() -> callback.onError("No se pudo consumir del inventario"));
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> callback.onError("Error al consumir item: " + e.getMessage()));
            }
        });
    }

    /**
     * Actualiza el costo unitario de un item.
     */
    public void actualizarCosto(int itemId, double nuevoCosto, InventarioCallback callback) {
        executorService.execute(() -> {
            try {
                InventarioItem item = inventarioRepository.obtenerItem(itemId);

                if (item == null) {
                    activity.runOnUiThread(() -> callback.onError("Item no encontrado"));
                    return;
                }

                item.setCostoUnitario(nuevoCosto);
                inventarioRepository.guardarItem(item);

                activity.runOnUiThread(() -> callback.onOperacionExitosa("Costo actualizado"));
            } catch (Exception e) {
                activity.runOnUiThread(() -> callback.onError("Error al actualizar costo: " + e.getMessage()));
            }
        });
    }

    /**
     * Elimina un item del inventario.
     */
    public void eliminarItem(int itemId, InventarioCallback callback) {
        executorService.execute(() -> {
            try {
                InventarioItem item = inventarioRepository.obtenerItem(itemId);

                if (item == null) {
                    activity.runOnUiThread(() -> callback.onError("Item no encontrado"));
                    return;
                }

                if (item.getCantidad() > 0) {
                    activity.runOnUiThread(() -> callback.onError("No se puede eliminar: aún tiene stock disponible"));
                    return;
                }

                inventarioRepository.eliminarItem(item);

                activity.runOnUiThread(() -> callback.onOperacionExitosa("Item eliminado"));
            } catch (Exception e) {
                activity.runOnUiThread(() -> callback.onError("Error al eliminar item: " + e.getMessage()));
            }
        });
    }

    /**
     * Obtiene las categorias disponibles.
     */
    public String[] obtenerCategorias() {
        return new String[] { "PESTICIDA", "FERTILIZANTE", "SEMILLA", "OTRO" };
    }

    /**
     * Obtiene las unidades de medida disponibles.
     */
    public String[] obtenerUnidades() {
        return new String[] { "KG", "LITROS", "GALONES", "ML", "PAQUETES", "SACOS" };
    }

    private String obtenerFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Muestra diálogo para consumir stock de un item.
     */
    public void mostrarDialogConsumir(android.content.Context context, final InventarioItem item,
            final InventarioCallback callback) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Consumir " + item.getNombre());

        android.view.View view = ((android.app.Activity) context).getLayoutInflater()
                .inflate(com.agtech.nepenya.R.layout.dialog_cantidad, null);
        android.widget.TextView tvInfo = view.findViewById(
                context.getResources().getIdentifier("tv_info", "id", context.getPackageName()));
        android.widget.TextView tvCantidad = view.findViewById(
                context.getResources().getIdentifier("tv_cantidad", "id", context.getPackageName()));

        tvInfo.setText(String.format("Stock disponible: %.2f %s", item.getCantidad(), item.getUnidad()));
        tvCantidad.setText("1.0");

        // Botones + y -
        view.findViewById(context.getResources().getIdentifier("btn_menos", "id", context.getPackageName()))
                .setOnClickListener(v -> {
                    double cant = Double.parseDouble(tvCantidad.getText().toString());
                    if (cant > 0.5) {
                        tvCantidad.setText(String.valueOf(cant - 0.5));
                    }
                });

        view.findViewById(context.getResources().getIdentifier("btn_mas", "id", context.getPackageName()))
                .setOnClickListener(v -> {
                    double cant = Double.parseDouble(tvCantidad.getText().toString());
                    if (cant < item.getCantidad()) {
                        tvCantidad.setText(String.valueOf(cant + 0.5));
                    }
                });

        builder.setView(view);
        builder.setPositiveButton("Consumir", (dialog, which) -> {
            double cantidad = Double.parseDouble(tvCantidad.getText().toString());
            consumirItem(item.getId(), cantidad, callback);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
