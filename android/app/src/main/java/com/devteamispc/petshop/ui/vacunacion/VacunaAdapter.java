package com.devteamispc.petshop.ui.vacunacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.model.Vacuna;
import com.devteamispc.petshop.databinding.ItemVacunaBinding;
import com.devteamispc.petshop.util.Badge;

import java.util.ArrayList;
import java.util.List;

/** Fila del catálogo de vacunas: nombre, frecuencia y descripción. */
public class VacunaAdapter extends RecyclerView.Adapter<VacunaAdapter.Holder> {

    public interface AlTocar {
        void vacuna(Vacuna vacuna);
    }

    private final List<Vacuna> datos = new ArrayList<>();
    private final AlTocar alTocar;

    public VacunaAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void actualizar(List<Vacuna> nuevas) {
        datos.clear();
        datos.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Holder(ItemVacunaBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
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

        private final ItemVacunaBinding v;

        Holder(ItemVacunaBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Vacuna x) {
            v.nombre.setText(x.getNombre());
            v.frecuencia.setText(x.getFrecuencia());
            Badge.pintar(v.frecuencia, R.color.azul_claro, R.color.azul_oscuro);
            String d = x.getDescripcion();
            v.descripcion.setVisibility(d == null || d.isEmpty() ? View.GONE : View.VISIBLE);
            v.descripcion.setText(d);
            v.getRoot().setOnClickListener(c -> alTocar.vacuna(x));
        }
    }
}
