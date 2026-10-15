package com.devteamispc.petshop.ui.carnet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.model.Vacunacion;
import com.devteamispc.petshop.databinding.ItemVacunacionBinding;
import com.devteamispc.petshop.util.Badge;
import com.devteamispc.petshop.util.Estados;
import com.devteamispc.petshop.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/** Pestaña Vacunas del carnet: cada vacuna aplicada con su próxima dosis. */
public class VacunacionAdapter extends RecyclerView.Adapter<VacunacionAdapter.Holder> {

    private final List<Vacunacion> datos = new ArrayList<>();

    public void actualizar(List<Vacunacion> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Holder(ItemVacunacionBinding.inflate(
                LayoutInflater.from(padre.getContext()), padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int posicion) {
        holder.pintar(datos.get(posicion));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        private final ItemVacunacionBinding v;

        Holder(ItemVacunacionBinding binding) {
            super(binding.getRoot());
            this.v = binding;
        }

        void pintar(Vacunacion x) {
            Context c = v.getRoot().getContext();
            v.vacuna.setText(x.getVacunaNombre());

            v.aplicada.setText(c.getString(R.string.carnet_aplicada, Fechas.aVisible(x.getFechaAplicacion())));
            String vet = x.getVeterinarioNombre();
            v.aplico.setVisibility(vet == null || vet.isEmpty() ? View.GONE : View.VISIBLE);
            v.aplico.setText(c.getString(R.string.carnet_aplico, vet));

            if (x.getProximaDosis() != null) {
                v.proxima.setText(c.getString(R.string.carnet_proxima,
                        Fechas.aVisible(x.getProximaDosis())));
                v.badge.setVisibility(View.VISIBLE);
                v.badge.setText(Fechas.textoDias(x.getDiasParaProximaDosis()));
                Badge.pintar(v.badge, Estados.nivelDosis(x.getDiasParaProximaDosis()));
            } else {
                v.proxima.setText(R.string.carnet_sin_proxima);
                v.badge.setVisibility(View.GONE);
            }
        }
    }
}
