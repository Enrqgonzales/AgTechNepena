package com.agtech.nepenya.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agtech.nepenya.R;
import com.agtech.nepenya.model.entity.Registro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter para mostrar registros en RecyclerView.
 * Soporta swipe-to-delete con callback.
 *
 * @author AgTech Nepeña Team
 * @version 1.0
 */
public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.ViewHolder> {

    private final List<Registro> registros;
    private final OnRegistroClickListener listener;
    private final SimpleDateFormat inputFormat;

    /**
     * Interface para clicks en registros.
     */
    public interface OnRegistroClickListener {
        void onRegistroClick(Registro registro);
        void onRegistroEliminar(Registro registro);
    }

    /**
     * ViewHolder para item de registro.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View dotIndicator;
        public final TextView tvCategoria;
        public final TextView tvFecha;
        public final TextView tvMonto;
        public final ImageView ivSync;

        public ViewHolder(View view) {
            super(view);
            dotIndicator = view.findViewById(R.id.dot_indicator);
            tvCategoria = view.findViewById(R.id.tv_categoria);
            tvFecha = view.findViewById(R.id.tv_fecha);
            tvMonto = view.findViewById(R.id.tv_monto);
            ivSync = view.findViewById(R.id.iv_sync);
        }
    }

    /**
     * Constructor del adapter.
     */
    public RegistroAdapter(List<Registro> registros, OnRegistroClickListener listener) {
        this.registros = new ArrayList<>(registros);
        this.listener = listener;
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Registro registro = registros.get(position);

        // Color del indicador (rojo para gasto, verde para ingreso)
        int dotColor = registro.esGasto()
                ? holder.itemView.getContext().getColor(R.color.error_red)
                : holder.itemView.getContext().getColor(R.color.accent_green);
        holder.dotIndicator.setBackgroundColor(dotColor);

        // Categoria y fecha
        holder.tvCategoria.setText(registro.getCategoria());
        holder.tvFecha.setText(formatearFecha(registro.getFecha()));

        // Monto con color segun tipo
        String montoStr = String.format(Locale.getDefault(), "S/ %,.2f", registro.getMonto());
        holder.tvMonto.setText(montoStr);
        int montoColor = registro.esGasto()
                ? holder.itemView.getContext().getColor(R.color.error_red)
                : holder.itemView.getContext().getColor(R.color.accent_green);
        holder.tvMonto.setTextColor(montoColor);

        // Icono de sincronizacion
        if ("PENDING".equals(registro.getSyncStatus())) {
            holder.ivSync.setImageResource(R.drawable.ic_sync_pending);
            holder.ivSync.setVisibility(View.VISIBLE);
        } else {
            holder.ivSync.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRegistroClick(registro);
            }
        });
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    /**
     * Actualiza la lista de registros.
     */
    public void actualizarLista(List<Registro> nuevosRegistros) {
        registros.clear();
        registros.addAll(nuevosRegistros);
        notifyDataSetChanged();
    }

    /**
     * Elimina un registro por posicion.
     */
    public void eliminarRegistro(int position) {
        if (position >= 0 && position < registros.size()) {
            Registro registro = registros.get(position);
            registros.remove(position);
            notifyItemRemoved(position);
            if (listener != null) {
                listener.onRegistroEliminar(registro);
            }
        }
    }

    /**
     * Obtiene registro en posicion.
     */
    public Registro getRegistro(int position) {
        if (position >= 0 && position < registros.size()) {
            return registros.get(position);
        }
        return null;
    }

    /**
     * Formatea fecha para visualizacion.
     */
    private String formatearFecha(String fecha) {
        try {
            Date date = inputFormat.parse(fecha);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                String month = getNombreMes(cal.get(Calendar.MONTH));
                return day + " " + month;
            }
        } catch (ParseException e) {
            // Fallback
        }
        return fecha;
    }

    private String getNombreMes(int mes) {
        String[] meses = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
        return meses[mes];
    }
}
