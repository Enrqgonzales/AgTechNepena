package com.agtech.nepenya.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agtech.nepenya.R;
import com.agtech.nepenya.model.entity.InventarioItem;

import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar items de inventario en RecyclerView.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class InventarioAdapter extends RecyclerView.Adapter<InventarioAdapter.ViewHolder> {

    private List<InventarioItem> items;
    private OnInventarioClickListener listener;

    public interface OnInventarioClickListener {
        void onItemClick(InventarioItem item);

        void onConsumirClick(InventarioItem item);
    }

    public InventarioAdapter(List<InventarioItem> items, OnInventarioClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventarioItem item = items.get(position);

        holder.tvNombre.setText(item.getNombre());
        holder.tvCategoria.setText(item.getCategoria());
        holder.tvStock.setText(String.format(Locale.getDefault(), "%.2f %s",
                item.getCantidad(), item.getUnidad()));
        holder.tvCosto.setText(String.format(Locale.getDefault(), "S/ %.2f/u",
                item.getCostoUnitario()));
        holder.tvValor.setText(String.format(Locale.getDefault(), "Total: S/ %.2f",
                item.getCostoTotal()));

        // Color según stock
        if (item.getCantidad() <= 0) {
            holder.tvStock.setTextColor(holder.itemView.getContext().getColor(R.color.error_red));
            holder.btnConsumir.setEnabled(false);
        } else {
            holder.tvStock.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
            holder.btnConsumir.setEnabled(true);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onItemClick(item);
        });

        holder.btnConsumir.setOnClickListener(v -> {
            if (listener != null && item.getCantidad() > 0) {
                listener.onConsumirClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void actualizarLista(List<InventarioItem> nuevosItems) {
        this.items = nuevosItems;
        notifyDataSetChanged();
    }

    public InventarioItem getItem(int position) {
        return items.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCategoria, tvStock, tvCosto, tvValor;
        Button btnConsumir;
        LinearLayout layoutActions;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvCategoria = itemView.findViewById(R.id.tv_categoria);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvCosto = itemView.findViewById(R.id.tv_costo);
            tvValor = itemView.findViewById(R.id.tv_valor);
            btnConsumir = itemView.findViewById(R.id.btn_consumir);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }
    }
}
