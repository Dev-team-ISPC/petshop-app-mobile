package com.devteamispc.petshop.ui.agenda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.model.Agenda;
import com.devteamispc.petshop.data.model.Turno;
import com.devteamispc.petshop.databinding.ItemAgendaDosisBinding;
import com.devteamispc.petshop.databinding.ItemAgendaTituloBinding;
import com.devteamispc.petshop.databinding.ItemAgendaVacioBinding;
import com.devteamispc.petshop.databinding.ItemTurnoGestionBinding;
import com.devteamispc.petshop.util.Badge;
import com.devteamispc.petshop.util.Estados;
import com.devteamispc.petshop.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/**
 * Agenda del personal: lista de turnos (con su título de sección para el
 * administrador) y, para el administrador, las próximas dosis de la clínica.
 *
 * El veterinario confirma o cancela los pendientes desde la tarjeta; tocándola
 * se abre el diálogo con todas las acciones (lo resuelve AgendaActivity).
 */
public class GestionTurnosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Acciones {
        void abrir(Turno turno);
        void confirmar(Turno turno);
        void cancelar(Turno turno);
        void abrirCarnet(int mascotaId, String mascotaNombre);
    }

    private static final int TIPO_TITULO = 0;
    private static final int TIPO_VACIO = 1;
    private static final int TIPO_TURNO = 2;
    private static final int TIPO_DOSIS = 3;

    private static final class Texto {
        final int tipo;
        @StringRes final int texto;
        Texto(int tipo, @StringRes int texto) { this.tipo = tipo; this.texto = texto; }
    }

    private final List<Object> filas = new ArrayList<>();
    private final boolean esVeterinario;
    private final Acciones acciones;
    private int turnoOcupado = -1;

    public GestionTurnosAdapter(boolean esVeterinario, Acciones acciones) {
        this.esVeterinario = esVeterinario;
        this.acciones = acciones;
    }

    /**
     * @param tituloTurnos título de la sección de turnos, o 0 para no mostrarlo
     * @param dosis        próximas dosis (sólo admin), o null para no mostrar la sección
     */
    public void actualizar(@StringRes int tituloTurnos, List<Turno> turnos, @StringRes int vacioTurnos,
                           List<Agenda.ProximaDosis> dosis) {
        filas.clear();
        if (tituloTurnos != 0) filas.add(new Texto(TIPO_TITULO, tituloTurnos));
        if (turnos.isEmpty()) {
            filas.add(new Texto(TIPO_VACIO, vacioTurnos));
        } else {
            filas.addAll(turnos);
        }
        if (dosis != null) {
            filas.add(new Texto(TIPO_TITULO, R.string.agenda_titulo_dosis));
            if (dosis.isEmpty()) {
                filas.add(new Texto(TIPO_VACIO, R.string.agenda_vacio_dosis));
            } else {
                filas.addAll(dosis);
            }
        }
        notifyDataSetChanged();
    }

    public void marcarOcupado(int turnoId) {
        turnoOcupado = turnoId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int posicion) {
        Object fila = filas.get(posicion);
        if (fila instanceof Texto) return ((Texto) fila).tipo;
        if (fila instanceof Turno) return TIPO_TURNO;
        return TIPO_DOSIS;
    }

    @Override
    public int getItemCount() {
        return filas.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        LayoutInflater inflater = LayoutInflater.from(padre.getContext());
        switch (tipo) {
            case TIPO_TITULO: return new TituloHolder(ItemAgendaTituloBinding.inflate(inflater, padre, false));
            case TIPO_VACIO: return new VacioHolder(ItemAgendaVacioBinding.inflate(inflater, padre, false));
            case TIPO_TURNO: return new TurnoHolder(ItemTurnoGestionBinding.inflate(inflater, padre, false));
            default: return new DosisHolder(ItemAgendaDosisBinding.inflate(inflater, padre, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int posicion) {
        Object fila = filas.get(posicion);
        if (holder instanceof TituloHolder) ((TituloHolder) holder).pintar((Texto) fila);
        else if (holder instanceof VacioHolder) ((VacioHolder) holder).pintar((Texto) fila);
        else if (holder instanceof TurnoHolder) ((TurnoHolder) holder).pintar((Turno) fila);
        else ((DosisHolder) holder).pintar((Agenda.ProximaDosis) fila);
    }

    static class TituloHolder extends RecyclerView.ViewHolder {
        private final ItemAgendaTituloBinding v;

        TituloHolder(ItemAgendaTituloBinding binding) {
            super(binding.getRoot());
            v = binding;
            ViewCompat.setAccessibilityHeading(v.getRoot(), true);
        }

        void pintar(Texto t) { v.getRoot().setText(t.texto); }
    }

    static class VacioHolder extends RecyclerView.ViewHolder {
        private final ItemAgendaVacioBinding v;

        VacioHolder(ItemAgendaVacioBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Texto t) { v.getRoot().setText(t.texto); }
    }

    class TurnoHolder extends RecyclerView.ViewHolder {
        private final ItemTurnoGestionBinding v;

        TurnoHolder(ItemTurnoGestionBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Turno t) {
            Context c = v.getRoot().getContext();
            String dueno = t.getDuenoNombre() == null ? "" : t.getDuenoNombre();
            if (esVeterinario) {
                v.titulo.setText(t.getServicioNombre());
                v.detalle.setText(t.getMascotaNombre() + " · " + dueno);
            } else {
                // Administrador: toda la clínica, con quién atiende.
                v.titulo.setText(t.getServicioNombre() + " · " + t.getMascotaNombre());
                String vet = t.getVeterinarioNombre();
                v.detalle.setText(c.getString(R.string.agenda_detalle_admin, dueno,
                        vet == null || vet.isEmpty() ? c.getString(R.string.agenda_sin_veterinario) : vet));
            }
            v.fecha.setText(Fechas.fechaHoraAVisible(t.getFecha()));
            v.estado.setText(t.getEstadoDisplay() != null ? t.getEstadoDisplay() : Estados.etiquetaEstado(t.getEstado()));
            Badge.pintar(v.estado, Estados.nivelTurno(t.getEstado()));
            v.getRoot().setBackgroundResource(t.estaCerrado() ? R.drawable.fondo_tarjeta_cerrada : R.drawable.fondo_tarjeta);

            // Atajo en la tarjeta: confirmar o cancelar un pendiente.
            boolean atajo = Estados.puedeConfirmar(esVeterinario, t.getEstado());
            boolean ocupado = turnoOcupado == t.getId();
            v.acciones.setVisibility(atajo ? View.VISIBLE : View.GONE);
            v.botonConfirmar.setEnabled(!ocupado);
            v.botonCancelar.setEnabled(!ocupado);
            v.botonConfirmar.setContentDescription(c.getString(R.string.desc_confirmar_turno, t.getMascotaNombre()));
            v.botonCancelar.setContentDescription(c.getString(R.string.desc_cancelar_turno, t.getMascotaNombre()));
            v.botonConfirmar.setOnClickListener(x -> acciones.confirmar(t));
            v.botonCancelar.setOnClickListener(x -> acciones.cancelar(t));
            v.getRoot().setOnClickListener(x -> acciones.abrir(t));
        }
    }

    class DosisHolder extends RecyclerView.ViewHolder {
        private final ItemAgendaDosisBinding v;

        DosisHolder(ItemAgendaDosisBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Agenda.ProximaDosis d) {
            v.titulo.setText(d.getVacunaNombre() + " · " + d.getMascotaNombre());
            v.fecha.setText(Fechas.aVisible(d.getFecha()));
            Badge.pintarDias(v.cajaDias, v.diasNumero, v.diasEtiqueta, d.getDiasRestantes());
            v.getRoot().setOnClickListener(x -> acciones.abrirCarnet(d.getMascotaId(), d.getMascotaNombre()));
        }
    }
}
