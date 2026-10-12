package com.devteamispc.petshop.ui.carnet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.model.Turno;
import com.devteamispc.petshop.databinding.ItemTurnoBinding;
import com.devteamispc.petshop.util.Badge;
import com.devteamispc.petshop.util.Estados;
import com.devteamispc.petshop.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/**
 * Pestaña Turnos del carnet: historial y turnos futuros de la mascota.
 * Los cerrados (completados o cancelados) se muestran con fondo gris, sin
 * bajar el contraste del texto. El cliente puede cancelar los suyos desde acá.
 */
public class TurnoAdapter extends RecyclerView.Adapter<TurnoAdapter.Holder> {

    public interface AlCancelar {
        void turno(Turno turno);
    }

    private final List<Turno> datos = new ArrayList<>();
    private final boolean puedeCancelar;
    private final AlCancelar alCancelar;
    private int turnoOcupado = -1;

    /**
     * @param puedeCancelar true sólo para el cliente: el veterinario gestiona
     *                      los turnos desde la agenda.
     */
    public TurnoAdapter(boolean puedeCancelar, AlCancelar alCancelar) {
        this.puedeCancelar = puedeCancelar;
        this.alCancelar = alCancelar;
    }

    public void actualizar(List<Turno> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    /** Deshabilita el botón de un turno mientras se cancela. -1 para ninguno. */
    public void marcarOcupado(int turnoId) {
        turnoOcupado = turnoId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Holder(ItemTurnoBinding.inflate(LayoutInflater.from(padre.getContext()), padre, false));
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

        private final ItemTurnoBinding v;

        Holder(ItemTurnoBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Turno t) {
            Context c = v.getRoot().getContext();
            v.servicio.setText(t.getServicioNombre());

            String fecha = Fechas.fechaHoraAVisible(t.getFecha());
            if (!t.estaCerrado() && t.getDiasRestantes() >= 0) {
                fecha += " · " + Fechas.textoDias(t.getDiasRestantes());
            }
            v.fecha.setText(fecha);

            String vet = t.getVeterinarioNombre();
            v.detalle.setVisibility(vet != null && !vet.isEmpty() ? View.VISIBLE : View.GONE);
            v.detalle.setText(c.getString(R.string.carnet_atiende, vet));

            String obs = t.getObservaciones();
            v.observaciones.setVisibility(obs != null && !obs.isEmpty() ? View.VISIBLE : View.GONE);
            v.observaciones.setText(obs);

            v.estado.setText(t.getEstadoDisplay() != null ? t.getEstadoDisplay() : Estados.etiquetaEstado(t.getEstado()));
            Badge.pintar(v.estado, Estados.nivelTurno(t.getEstado()));

            // Cerrado: fondo gris. El estado lo dice el badge, no la transparencia.
            v.getRoot().setBackgroundResource(t.estaCerrado() ? R.drawable.fondo_tarjeta_cerrada : R.drawable.fondo_tarjeta);

            boolean cancelable = puedeCancelar && Estados.puedeCancelar(false, true, t.getEstado());
            v.botonCancelar.setVisibility(cancelable ? View.VISIBLE : View.GONE);
            v.botonCancelar.setEnabled(turnoOcupado != t.getId());
            v.botonCancelar.setContentDescription(c.getString(R.string.desc_cancelar_turno_servicio, t.getServicioNombre(), fecha));
            v.botonCancelar.setOnClickListener(x -> alCancelar.turno(t));
        }
    }
}
