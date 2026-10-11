package com.devteamispc.petshop.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.databinding.ItemUsuarioBinding;
import com.devteamispc.petshop.util.Badge;
import com.devteamispc.petshop.util.Estados;

import java.util.ArrayList;
import java.util.List;

/** Fila de la lista de usuarios: inicial, nombre, email, rol y si está inactivo. */
public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.Holder> {

    public interface AlTocar {
        void usuario(Usuario usuario);
    }

    private final List<Usuario> datos = new ArrayList<>();
    private final AlTocar alTocar;

    public UsuarioAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void actualizar(List<Usuario> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Holder(ItemUsuarioBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
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

        private final ItemUsuarioBinding v;

        Holder(ItemUsuarioBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Usuario u) {
            String nombre = u.getNombre() == null ? "" : u.getNombre().trim();
            v.avatar.setText(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase());
            v.nombre.setText(nombre);
            v.email.setText(u.getEmail());

            v.rol.setText(u.getRolDisplay() != null ? u.getRolDisplay() : Estados.etiquetaEstado(u.getRol()));
            // Colores del mockup: admin violeta, veterinario verde, cliente gris.
            if (u.esAdmin()) {
                Badge.pintar(v.rol, R.color.rol_admin_fondo, R.color.rol_admin_texto);
            } else {
                Badge.pintar(v.rol, u.esVeterinario() ? Estados.Nivel.OK : Estados.Nivel.NEUTRO);
            }

            boolean inactivo = u.getActivo() != null && !u.getActivo();
            v.inactivo.setVisibility(inactivo ? View.VISIBLE : View.GONE);
            if (inactivo) Badge.pintar(v.inactivo, Estados.Nivel.PELIGRO);

            v.getRoot().setOnClickListener(x -> alTocar.usuario(u));
        }
    }
}
