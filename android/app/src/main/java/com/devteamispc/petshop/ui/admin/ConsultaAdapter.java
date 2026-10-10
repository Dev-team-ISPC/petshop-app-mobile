package com.devteamispc.petshop.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.model.Consulta;
import com.devteamispc.petshop.databinding.ItemConsultaBinding;
import com.devteamispc.petshop.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/** Mensaje de contacto. Las sin leer llevan un punto azul y el botón para marcarlas. */
public class ConsultaAdapter extends RecyclerView.Adapter<ConsultaAdapter.Holder> {

    public interface Acciones {
        void marcarLeida(Consulta consulta);
        void responder(Consulta consulta);
    }

    private final List<Consulta> datos = new ArrayList<>();
    private final Acciones acciones;
    private int ocupada = -1;

    public ConsultaAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    public void actualizar(List<Consulta> nuevas) {
        datos.clear();
        datos.addAll(nuevas);
        notifyDataSetChanged();
    }

    public void marcarOcupada(int id) {
        ocupada = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Holder(ItemConsultaBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int posicion) {
        holder.pintar(datos.get(posicion));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        private final ItemConsultaBinding v;

        Holder(ItemConsultaBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Consulta c) {
            Context ctx = v.getRoot().getContext();
            boolean sinLeer = !c.isLeida();
            v.punto.setVisibility(sinLeer ? View.VISIBLE : View.GONE);
            v.nombre.setText(c.getNombre());
            // El estado no depende sólo del punto azul: TalkBack lo dice con texto.
            v.nombre.setContentDescription(sinLeer
                    ? ctx.getString(R.string.desc_consulta_sin_leer, c.getNombre())
                    : c.getNombre());
            v.fecha.setText(Fechas.fechaHoraRelativa(c.getCreadoEn()));
            v.email.setText(c.getEmail());
            v.email.setContentDescription(ctx.getString(R.string.desc_responder_a, c.getEmail()));
            v.mensaje.setText(c.getMensaje());

            v.getRoot().setBackgroundResource(sinLeer ? R.drawable.fondo_tarjeta : R.drawable.fondo_tarjeta_cerrada);
            v.botonLeida.setVisibility(sinLeer ? View.VISIBLE : View.GONE);
            v.botonLeida.setEnabled(ocupada != c.getId());
            v.botonLeida.setOnClickListener(x -> acciones.marcarLeida(c));
            v.email.setOnClickListener(x -> acciones.responder(c));
        }
    }
}
