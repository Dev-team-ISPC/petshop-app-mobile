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
import com.devteamispc.petshop.databinding.ItemAgendaDosisBinding;
import com.devteamispc.petshop.databinding.ItemAgendaTituloBinding;
import com.devteamispc.petshop.databinding.ItemAgendaTurnoBinding;
import com.devteamispc.petshop.databinding.ItemAgendaVacioBinding;
import com.devteamispc.petshop.util.Badge;
import com.devteamispc.petshop.util.Estados;
import com.devteamispc.petshop.util.Fechas;

import java.util.ArrayList;
import java.util.List;

/**
 * Una sola lista con varios tipos de fila (view types):
 *
 *   PRÓXIMAS DOSIS        <- título de sección
 *   [dosis] [dosis]       <- o "Sin dosis próximas."
 *   PRÓXIMOS TURNOS       <- título de sección
 *   [turno] [turno]       <- o "Sin turnos próximos."
 *
 * Es más eficiente que dos RecyclerView dentro de un ScrollView y mantiene un
 * solo desplazamiento y un solo "deslizar para recargar".
 */
public class AgendaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Acciones {
        void abrirCarnet(int mascotaId, String mascotaNombre);
        void confirmar(Agenda.TurnoAgenda turno);
        void cancelar(Agenda.TurnoAgenda turno);
    }

    /** Valor de marcarOcupado() cuando no hay ningún turno guardándose. */
    public static final int NINGUNO = -1;

    private static final int TIPO_TITULO = 0;
    private static final int TIPO_VACIO = 1;
    private static final int TIPO_DOSIS = 2;
    private static final int TIPO_TURNO = 3;

    /** Filas que no son datos: un título o un aviso de sección vacía. */
    private static final class Texto {
        final int tipo;
        @StringRes final int texto;
        Texto(int tipo, @StringRes int texto) { this.tipo = tipo; this.texto = texto; }
    }

    private final List<Object> filas = new ArrayList<>();
    private final boolean esVeterinario;
    private final boolean esCliente;
    private final Acciones acciones;
    private int turnoOcupado = NINGUNO;

    public AgendaAdapter(boolean esVeterinario, boolean esCliente, Acciones acciones) {
        this.esVeterinario = esVeterinario;
        this.esCliente = esCliente;
        this.acciones = acciones;
    }

    public void actualizar(Agenda agenda) {
        filas.clear();

        filas.add(new Texto(TIPO_TITULO, R.string.agenda_titulo_dosis));
        if (agenda.getProximasDosis().isEmpty()) {
            filas.add(new Texto(TIPO_VACIO, R.string.agenda_vacio_dosis));
        } else {
            filas.addAll(agenda.getProximasDosis());
        }

        filas.add(new Texto(TIPO_TITULO, R.string.agenda_titulo_turnos));
        if (agenda.getTurnos().isEmpty()) {
            filas.add(new Texto(TIPO_VACIO, R.string.agenda_vacio_turnos));
        } else {
            filas.addAll(agenda.getTurnos());
        }

        notifyDataSetChanged();
    }

    /** Deshabilita los botones de un turno mientras se guarda el cambio. */
    public void marcarOcupado(int turnoId) {
        turnoOcupado = turnoId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int posicion) {
        Object fila = filas.get(posicion);
        if (fila instanceof Texto) return ((Texto) fila).tipo;
        if (fila instanceof Agenda.ProximaDosis) return TIPO_DOSIS;
        return TIPO_TURNO;
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
            case TIPO_TITULO:
                return new TituloHolder(ItemAgendaTituloBinding.inflate(inflater, padre, false));
            case TIPO_VACIO:
                return new VacioHolder(ItemAgendaVacioBinding.inflate(inflater, padre, false));
            case TIPO_DOSIS:
                return new DosisHolder(ItemAgendaDosisBinding.inflate(inflater, padre, false));
            default:
                return new TurnoHolder(ItemAgendaTurnoBinding.inflate(inflater, padre, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int posicion) {
        Object fila = filas.get(posicion);
        if (holder instanceof TituloHolder) {
            ((TituloHolder) holder).pintar((Texto) fila);
        } else if (holder instanceof VacioHolder) {
            ((VacioHolder) holder).pintar((Texto) fila);
        } else if (holder instanceof DosisHolder) {
            ((DosisHolder) holder).pintar((Agenda.ProximaDosis) fila);
        } else {
            ((TurnoHolder) holder).pintar((Agenda.TurnoAgenda) fila);
        }
    }

    // ------------------------------------------------------------- holders

    static class TituloHolder extends RecyclerView.ViewHolder {
        private final ItemAgendaTituloBinding v;

        TituloHolder(ItemAgendaTituloBinding binding) {
            super(binding.getRoot());
            v = binding;
            // TalkBack lo anuncia como encabezado y permite saltar entre secciones.
            ViewCompat.setAccessibilityHeading(v.getRoot(), true);
        }

        void pintar(Texto t) {
            v.getRoot().setText(t.texto);
        }
    }

    static class VacioHolder extends RecyclerView.ViewHolder {
        private final ItemAgendaVacioBinding v;

        VacioHolder(ItemAgendaVacioBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Texto t) {
            v.getRoot().setText(t.texto);
        }
    }

    class DosisHolder extends RecyclerView.ViewHolder {
        private final ItemAgendaDosisBinding v;

        DosisHolder(ItemAgendaDosisBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Agenda.ProximaDosis d) {
            v.titulo.setText(d.getVacunaNombre());
            v.fecha.setText(d.getMascotaNombre() + " · " + Fechas.aVisible(d.getFecha()));
            Badge.pintarDias(v.cajaDias, v.diasNumero, v.diasEtiqueta, d.getDiasRestantes());
            v.getRoot().setOnClickListener(x ->
                    acciones.abrirCarnet(d.getMascotaId(), d.getMascotaNombre()));
        }
    }

    class TurnoHolder extends RecyclerView.ViewHolder {
        private final ItemAgendaTurnoBinding v;

        TurnoHolder(ItemAgendaTurnoBinding binding) {
            super(binding.getRoot());
            v = binding;
        }

        void pintar(Agenda.TurnoAgenda t) {
            Context c = v.getRoot().getContext();
            v.titulo.setText(t.getMascotaNombre() + " · " + t.getServicioNombre());
            v.fecha.setText(Fechas.fechaHoraAVisible(t.getFecha())
                    + " · " + Fechas.textoDias(t.getDiasRestantes()));
            v.estado.setText(Estados.etiquetaEstado(t.getEstado()));
            Badge.pintar(v.estado, Estados.nivelTurno(t.getEstado()));

            boolean confirmar = Estados.puedeConfirmar(esVeterinario, t.getEstado());
            boolean cancelar = Estados.puedeCancelar(esVeterinario, esCliente, t.getEstado());
            boolean ocupado = turnoOcupado == t.getTurnoId();

            v.acciones.setVisibility(confirmar || cancelar ? View.VISIBLE : View.GONE);
            v.botonConfirmar.setVisibility(confirmar ? View.VISIBLE : View.GONE);
            v.botonCancelar.setVisibility(cancelar ? View.VISIBLE : View.GONE);
            v.botonConfirmar.setEnabled(!ocupado);
            v.botonCancelar.setEnabled(!ocupado);

            // TalkBack: "Confirmar el turno de Rocco", no sólo "Confirmar".
            v.botonConfirmar.setContentDescription(c.getString(R.string.desc_confirmar_turno, t.getMascotaNombre()));
            v.botonCancelar.setContentDescription(c.getString(R.string.desc_cancelar_turno, t.getMascotaNombre()));

            v.botonConfirmar.setOnClickListener(x -> acciones.confirmar(t));
            v.botonCancelar.setOnClickListener(x -> acciones.cancelar(t));
            v.getRoot().setOnClickListener(x ->
                    acciones.abrirCarnet(t.getMascotaId(), t.getMascotaNombre()));
        }
    }
}
