package com.devteamispc.petshop.ui.mascota;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.data.model.Mascota;
import com.devteamispc.petshop.databinding.ItemMascotaBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter de la lista de mascotas. Sirve de molde para los demás:
 * ViewBinding en el ViewHolder, una interfaz para el click, y un método
 * actualizar() que reemplaza los datos.
 */
public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.Holder> {

    public interface AlTocar {
        void mascota(Mascota mascota);
    }

    private final List<Mascota> datos = new ArrayList<>();
    private final boolean mostrarDueno;
    private final AlTocar alTocar;

    public MascotaAdapter(boolean mostrarDueno, AlTocar alTocar) {
        this.mostrarDueno = mostrarDueno;
        this.alTocar = alTocar;
    }

    public void actualizar(List<Mascota> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        ItemMascotaBinding binding = ItemMascotaBinding.inflate(
                LayoutInflater.from(padre.getContext()), padre, false);
        return new Holder(binding);
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

        private final ItemMascotaBinding v;

        Holder(ItemMascotaBinding binding) {
            super(binding.getRoot());
            this.v = binding;
        }

        void pintar(Mascota m) {
            v.avatar.setText(m.getInicial());
            v.nombre.setText(m.getNombre());

            String detalle = m.getEspecieDisplay() + " · " + m.getRaza();
            if (m.getPeso() != null) {
                detalle += " · " + m.getPeso().replace('.', ',') + " kg";
            }
            v.detalle.setText(detalle);

            if (mostrarDueno && m.getDuenoNombre() != null) {
                v.dueno.setVisibility(View.VISIBLE);
                v.dueno.setText("Dueño: " + m.getDuenoNombre());
            } else {
                v.dueno.setVisibility(View.GONE);
            }

            v.getRoot().setOnClickListener(x -> alTocar.mascota(m));
        }
    }
}
