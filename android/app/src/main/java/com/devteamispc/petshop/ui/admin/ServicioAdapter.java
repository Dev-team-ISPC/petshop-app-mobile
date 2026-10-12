package com.devteamispc.petshop.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.model.Servicio;
import com.devteamispc.petshop.databinding.ItemServicioBinding;
import com.devteamispc.petshop.util.Badge;
import com.devteamispc.petshop.util.Estados;

import java.util.ArrayList;
import java.util.List;

/** Fila del catálogo de servicios: nombre, descripción, duración y estado. */
public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.Holder> {

    public interface AlTocar {
        void servicio(Servicio servicio);
    }

    private final List<Servicio> datos = new ArrayList<>();
    private final AlTocar alTocar;

    public ServicioAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void actualizar(List<Servicio> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Holder(ItemServicioBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
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

        private final ItemServicioBinding v;

        Holder(ItemServicioBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Servicio s) {
            Context c = v.getRoot().getContext();
            v.nombre.setText(s.getNombre());

            String descripcion = s.getDescripcion();
            v.descripcion.setVisibility(descripcion == null || descripcion.isEmpty() ? View.GONE : View.VISIBLE);
            v.descripcion.setText(descripcion);

            v.duracion.setText(c.getString(R.string.servicio_duracion, s.getDuracionMinutos()));

            v.estado.setText(s.isActivo() ? R.string.estado_activo : R.string.estado_inactivo);
            Badge.pintar(v.estado, s.isActivo() ? Estados.Nivel.OK : Estados.Nivel.NEUTRO);

            v.getRoot().setOnClickListener(x -> alTocar.servicio(s));
        }
    }
}
