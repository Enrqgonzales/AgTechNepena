package com.agtech.nepenya.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agtech.nepenya.R;
import com.agtech.nepenya.model.entity.Parcela;

import java.util.List;
import java.util.Locale;

/**
 * Adapter para la lista de parcelas en MisParcelasActivity.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class ParcelasAdapter extends RecyclerView.Adapter<ParcelasAdapter.ParcelaViewHolder> {

    private List<Parcela> parcelas;
    private final OnParcelaListener listener;

    public interface OnParcelaListener {
        void onParcelaClick(Parcela parcela);
        void onParcelaEliminar(Parcela parcela, int position);
    }

    public ParcelasAdapter(List<Parcela> parcelas, OnParcelaListener listener) {
        this.parcelas = parcelas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParcelaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parcela, parent, false);
        return new ParcelaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcelaViewHolder holder, int position) {
        Parcela parcela = parcelas.get(position);
        holder.tvNombre.setText(parcela.getNombre());
        holder.tvCultivo.setText(parcela.getCultivo());
        holder.tvHectareas.setText(String.format(Locale.getDefault(), "%.2f ha", parcela.getHectareas()));
        
        String estado = parcela.getEstado() != null ? parcela.getEstado() : "DISPONIBLE";
        holder.tvEstado.setText(estado);
        
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setCornerRadius(16f);
        
        if ("VENDIDA".equals(estado)) {
            int colorRed = holder.itemView.getContext().getColor(R.color.error_red);
            holder.tvEstado.setTextColor(colorRed);
            gd.setColor(colorRed & 0x1FFFFFFF);
            gd.setStroke(2, colorRed);
        } else {
            int colorGreen = holder.itemView.getContext().getColor(R.color.accent_green);
            holder.tvEstado.setTextColor(colorGreen);
            gd.setColor(colorGreen & 0x1FFFFFFF);
            gd.setStroke(2, colorGreen);
        }
        holder.tvEstado.setBackground(gd);

        holder.itemView.setOnClickListener(v -> listener.onParcelaClick(parcela));
    }

    @Override
    public int getItemCount() {
        return parcelas.size();
    }

    public void actualizarLista(List<Parcela> nuevas) {
        this.parcelas = nuevas;
        notifyDataSetChanged();
    }

    public void eliminarItem(int position) {
        parcelas.remove(position);
        notifyItemRemoved(position);
    }

    public Parcela getParcela(int position) {
        if (position >= 0 && position < parcelas.size()) {
            return parcelas.get(position);
        }
        return null;
    }

    static class ParcelaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvCultivo;
        TextView tvHectareas;
        TextView tvEstado;

        ParcelaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_parcela);
            tvCultivo = itemView.findViewById(R.id.tv_cultivo_parcela);
            tvHectareas = itemView.findViewById(R.id.tv_hectareas_parcela);
            tvEstado = itemView.findViewById(R.id.tv_estado_parcela);
        }
    }
}
