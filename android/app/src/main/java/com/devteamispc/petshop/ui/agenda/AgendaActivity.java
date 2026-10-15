package com.devteamispc.petshop.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.Agenda;
import com.devteamispc.petshop.data.model.CambioEstado;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Turno;
import com.devteamispc.petshop.databinding.ActivityAgendaBinding;
import com.devteamispc.petshop.databinding.DialogoTurnoBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.carnet.CarnetActivity;
import com.devteamispc.petshop.ui.turno.TurnoFormActivity;
import com.devteamispc.petshop.util.Estados;
import com.devteamispc.petshop.util.Fechas;
import com.devteamispc.petshop.util.SelectorFecha;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 9 · AgendaActivity — RF13, RF17, RF18. Una pantalla, tres modos según el rol.
 *
 * CLIENTE · "Agenda" · GET /agenda/
 *   Próximas dosis y turnos de sus mascotas. Puede cancelar los suyos.
 *   Si no hay nada, estado vacío con "Pedir un turno".
 *
 * VETERINARIO · "Turnos" · GET /turnos/?estado=…&futuros=1
 *   Filtros Pendientes / Confirmados / Todos. Confirma o cancela desde la
 *   tarjeta; tocándola: confirmar y tomar, marcar como completado o cancelar.
 *   Al confirmar, el backend lo asigna a quien confirma.
 *
 * ADMINISTRADOR · "Agenda general" · GET /turnos/ y GET /agenda/
 *   Filtros Todos / Pendientes / Hoy, y las próximas dosis de la clínica.
 *   No confirma ni completa (eso es del veterinario); puede eliminar un turno.
 *
 * Tocar una fila de dosis (o, para el cliente, de turno) abre el carnet.
 */
public class AgendaActivity extends BaseActivity implements AgendaAdapter.Acciones, GestionTurnosAdapter.Acciones {

    private enum Modo { CLIENTE, VETERINARIO, ADMIN }

    private ActivityAgendaBinding vista;
    private Modo modo;
    private AgendaAdapter adapterCliente;
    private GestionTurnosAdapter adapterGestion;

    /** Para el admin: los turnos y las dosis llegan en dos pedidos. */
    private List<Turno> turnosAdmin;
    private List<Agenda.ProximaDosis> dosisAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityAgendaBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        modo = sesion.esVeterinario() ? Modo.VETERINARIO : sesion.esAdmin() ? Modo.ADMIN : Modo.CLIENTE;
        vista.lista.setLayoutManager(new LinearLayoutManager(this));
        vista.refrescar.setOnRefreshListener(this::cargar);

        switch (modo) {
            case CLIENTE:
                setTitle(R.string.titulo_agenda);
                adapterCliente = new AgendaAdapter(false, true, this);
                vista.lista.setAdapter(adapterCliente);
                vista.botonPedirTurno.setOnClickListener(v ->
                        startActivity(new Intent(this, TurnoFormActivity.class)));
                break;
            case VETERINARIO:
                setTitle(R.string.titulo_turnos);
                configurarFiltros(R.string.filtro_pendientes, R.string.filtro_confirmados, R.string.filtro_todos);
                adapterGestion = new GestionTurnosAdapter(true, this);
                vista.lista.setAdapter(adapterGestion);
                break;
            default:
                setTitle(R.string.titulo_agenda_general);
                configurarFiltros(R.string.filtro_todos, R.string.filtro_pendientes, R.string.filtro_hoy);
                adapterGestion = new GestionTurnosAdapter(false, this);
                vista.lista.setAdapter(adapterGestion);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void configurarFiltros(int uno, int dos, int tres) {
        vista.barraFiltros.setVisibility(View.VISIBLE);
        vista.chipUno.setText(uno);
        vista.chipDos.setText(dos);
        vista.chipTres.setText(tres);
        vista.filtros.setOnCheckedStateChangeListener((grupo, ids) -> cargar());
    }

    private void cargar() {
        boolean vacia = vista.lista.getAdapter() == null || vista.lista.getAdapter().getItemCount() == 0;
        if (!vista.refrescar.isRefreshing() && vacia) mostrarEstado(Estado.CARGANDO);

        switch (modo) {
            case CLIENTE: cargarAgendaCliente(); break;
            case VETERINARIO: cargarTurnos(filtrosVeterinario()); break;
            default:
                turnosAdmin = null;
                dosisAdmin = null;
                cargarTurnos(filtrosAdmin());
                cargarDosisAdmin();
        }
    }

    // --------------------------------------------------------------- cliente

    private void cargarAgendaCliente() {
        ApiClient.getApi().agenda().enqueue(new Callback<Agenda>() {
            @Override
            public void onResponse(@NonNull Call<Agenda> call, @NonNull Response<Agenda> respuesta) {
                vista.refrescar.setRefreshing(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    Agenda agenda = respuesta.body();
                    adapterCliente.actualizar(agenda);
                    if (agenda.estaVacia()) {
                        mostrarVacio(R.string.agenda_vacia_titulo, R.string.agenda_vacia_texto, true);
                    } else {
                        mostrarEstado(Estado.CON_DATOS);
                    }
                } else {
                    mostrarError(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Agenda> call, @NonNull Throwable t) {
                vista.refrescar.setRefreshing(false);
                mostrarError(ApiError.sinConexion());
            }
        });
    }

    // -------------------------------------------------------- veterinario/admin

    private Map<String, String> filtrosVeterinario() {
        Map<String, String> f = new HashMap<>();
        int chip = vista.filtros.getCheckedChipId();
        if (chip == R.id.chipUno) f.put("estado", Turno.PENDIENTE);
        else if (chip == R.id.chipDos) f.put("estado", Turno.CONFIRMADO);
        else f.put("estado", Turno.PENDIENTE + "," + Turno.CONFIRMADO);
        f.put("futuros", "1");
        f.put("ordering", "fecha");
        f.put("page_size", "100");
        return f;
    }

    private Map<String, String> filtrosAdmin() {
        Map<String, String> f = new HashMap<>();
        int chip = vista.filtros.getCheckedChipId();
        if (chip == R.id.chipDos) {
            f.put("estado", Turno.PENDIENTE);
            f.put("futuros", "1");
        } else if (chip == R.id.chipTres) {
            String hoy = SelectorFecha.hoyApi();
            f.put("desde", hoy);
            f.put("hasta", hoy);
        } else {
            f.put("futuros", "1");
        }
        f.put("ordering", "fecha");
        f.put("page_size", "100");
        return f;
    }

    private void cargarTurnos(Map<String, String> filtros) {
        ApiClient.getApi().turnos(filtros).enqueue(new Callback<Pagina<Turno>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Turno>> call, @NonNull Response<Pagina<Turno>> respuesta) {
                vista.refrescar.setRefreshing(false);
                if (manejarErrorComun(respuesta)) return;
                if (!respuesta.isSuccessful() || respuesta.body() == null) {
                    mostrarError(ApiError.mensaje(respuesta));
                    return;
                }
                List<Turno> turnos = respuesta.body().getResults();
                if (modo == Modo.VETERINARIO) {
                    adapterGestion.actualizar(0, turnos, R.string.agenda_vacio_turnos_filtro, null);
                    if (turnos.isEmpty()) mostrarVacio(R.string.agenda_sin_turnos_titulo, R.string.agenda_vacio_turnos_filtro, false);
                    else mostrarEstado(Estado.CON_DATOS);
                } else {
                    turnosAdmin = turnos;
                    pintarAdmin();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Turno>> call, @NonNull Throwable t) {
                vista.refrescar.setRefreshing(false);
                mostrarError(ApiError.sinConexion());
            }
        });
    }

    private void cargarDosisAdmin() {
        ApiClient.getApi().agenda().enqueue(new Callback<Agenda>() {
            @Override
            public void onResponse(@NonNull Call<Agenda> call, @NonNull Response<Agenda> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    dosisAdmin = respuesta.body().getProximasDosis();
                    pintarAdmin();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Agenda> call, @NonNull Throwable t) {
                // Los turnos ya informan si no hay conexión.
            }
        });
    }

    /** Pinta cuando llegaron los dos pedidos del admin. */
    private void pintarAdmin() {
        if (turnosAdmin == null || dosisAdmin == null) return;
        adapterGestion.actualizar(R.string.agenda_titulo_clinica, turnosAdmin, R.string.agenda_vacio_turnos_filtro, dosisAdmin);
        mostrarEstado(Estado.CON_DATOS);
    }

    // --------------------------------------------------------------- acciones

    @Override
    public void abrirCarnet(int mascotaId, String mascotaNombre) {
        Intent i = new Intent(this, CarnetActivity.class);
        i.putExtra(CarnetActivity.EXTRA_MASCOTA_ID, mascotaId);
        i.putExtra(CarnetActivity.EXTRA_MASCOTA_NOMBRE, mascotaNombre);
        startActivity(i);
    }

    // Cliente (AgendaAdapter): turnos de la vista /agenda/

    @Override
    public void confirmar(Agenda.TurnoAgenda turno) {
        // El cliente no confirma; el adapter no muestra el botón.
    }

    @Override
    public void cancelar(Agenda.TurnoAgenda turno) {
        String detalle = turno.getServicioNombre() + " · " + turno.getMascotaNombre()
                + "\n" + Fechas.fechaHoraAVisible(turno.getFecha())
                + "\n\n" + getString(R.string.msg_cancelar_turno_cliente);
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_cancelar_turno)
                .setMessage(detalle)
                .setPositiveButton(R.string.accion_si_cancelar, (d, b) ->
                        cambiarEstado(turno.getTurnoId(), Turno.CANCELADO, R.string.agenda_cancelado))
                .setNegativeButton(R.string.accion_no_volver, null)
                .show();
    }

    // Personal (GestionTurnosAdapter)

    @Override
    public void confirmar(Turno turno) {
        cambiarEstado(turno.getId(), Turno.CONFIRMADO, R.string.agenda_confirmado);
    }

    @Override
    public void cancelar(Turno turno) {
        String detalle = turno.getServicioNombre() + " · " + turno.getMascotaNombre()
                + "\n" + Fechas.fechaHoraAVisible(turno.getFecha());
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_cancelar_turno)
                .setMessage(detalle)
                .setPositiveButton(R.string.accion_si_cancelar, (d, b) ->
                        cambiarEstado(turno.getId(), Turno.CANCELADO, R.string.agenda_cancelado))
                .setNegativeButton(R.string.accion_no_volver, null)
                .show();
    }

    /** Diálogo con las acciones que corresponden al rol y al estado del turno. */
    @Override
    public void abrir(Turno turno) {
        boolean vet = modo == Modo.VETERINARIO;
        DialogoTurnoBinding d = DialogoTurnoBinding.inflate(getLayoutInflater());
        d.servicio.setText(turno.getServicioNombre());
        d.detalle.setText(Fechas.fechaHoraAVisible(turno.getFecha())
                + (turno.getDuenoNombre() != null ? " · " + getString(R.string.carnet_dueno, turno.getDuenoNombre()) : ""));
        d.estadoActual.setText(getString(R.string.estado_actual,
                turno.getEstadoDisplay() != null ? turno.getEstadoDisplay() : Estados.etiquetaEstado(turno.getEstado())));

        boolean puedeConfirmar = Estados.puedeConfirmar(vet, turno.getEstado());
        boolean puedeCompletar = Estados.puedeCompletar(vet, turno.getEstado());
        boolean puedeCancelar = vet && Estados.puedeCancelar(true, false, turno.getEstado());
        boolean puedeEliminar = modo == Modo.ADMIN;

        d.botonConfirmar.setVisibility(puedeConfirmar ? View.VISIBLE : View.GONE);
        d.botonCompletar.setVisibility(puedeCompletar ? View.VISIBLE : View.GONE);
        d.botonCancelar.setVisibility(puedeCancelar ? View.VISIBLE : View.GONE);
        d.botonEliminar.setVisibility(puedeEliminar ? View.VISIBLE : View.GONE);
        d.nota.setText(vet ? R.string.nota_confirmar_asigna : R.string.nota_admin_turnos);

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.titulo_turno_de, turno.getMascotaNombre()))
                .setView(d.getRoot())
                .setNegativeButton(R.string.accion_cerrar, null)
                .create();

        d.botonConfirmar.setOnClickListener(v -> {
            dialogo.dismiss();
            cambiarEstado(turno.getId(), Turno.CONFIRMADO, R.string.agenda_confirmado);
        });
        d.botonCompletar.setOnClickListener(v -> {
            dialogo.dismiss();
            cambiarEstado(turno.getId(), Turno.COMPLETADO, R.string.agenda_completado);
        });
        d.botonCancelar.setOnClickListener(v -> {
            dialogo.dismiss();
            cancelar(turno);
        });
        d.botonEliminar.setOnClickListener(v -> {
            dialogo.dismiss();
            confirmarEliminar(turno);
        });
        dialogo.show();
    }

    private void confirmarEliminar(Turno turno) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar_turno)
                .setMessage(R.string.msg_eliminar_turno)
                .setPositiveButton(R.string.accion_eliminar, (d, b) -> eliminar(turno))
                .setNegativeButton(R.string.accion_volver, null)
                .show();
    }

    private void eliminar(Turno turno) {
        if (adapterGestion != null) adapterGestion.marcarOcupado(turno.getId());
        ApiClient.getApi().eliminarTurno(turno.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> respuesta) {
                if (adapterGestion != null) adapterGestion.marcarOcupado(-1);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.agenda_eliminado));
                    cargar();
                } else {
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (adapterGestion != null) adapterGestion.marcarOcupado(-1);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void cambiarEstado(int turnoId, String estado, int mensajeOk) {
        if (adapterGestion != null) adapterGestion.marcarOcupado(turnoId);
        if (adapterCliente != null) adapterCliente.marcarOcupado(turnoId);
        ApiClient.getApi().cambiarEstadoTurno(turnoId, new CambioEstado(estado)).enqueue(new Callback<Turno>() {
            @Override
            public void onResponse(@NonNull Call<Turno> call, @NonNull Response<Turno> respuesta) {
                liberar();
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(mensajeOk));
                    cargar();
                } else {
                    // Por ejemplo, 400 si otro usuario ya lo cerró.
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Turno> call, @NonNull Throwable t) {
                liberar();
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void liberar() {
        if (adapterGestion != null) adapterGestion.marcarOcupado(-1);
        if (adapterCliente != null) adapterCliente.marcarOcupado(AgendaAdapter.NINGUNO);
    }

    // ---------------------------------------------------------- presentación

    private enum Estado { CARGANDO, CON_DATOS }

    private void mostrarEstado(Estado estado) {
        vista.progreso.setVisibility(estado == Estado.CARGANDO ? View.VISIBLE : View.GONE);
        vista.lista.setVisibility(estado == Estado.CON_DATOS ? View.VISIBLE : View.GONE);
        vista.bloqueVacio.setVisibility(View.GONE);
    }

    private void mostrarVacio(int titulo, int texto, boolean conBotonTurno) {
        vista.progreso.setVisibility(View.GONE);
        vista.lista.setVisibility(View.GONE);
        vista.bloqueVacio.setVisibility(View.VISIBLE);
        vista.tituloVacio.setText(titulo);
        vista.textoVacio.setText(texto);
        vista.botonPedirTurno.setVisibility(conBotonTurno ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String mensaje) {
        vista.progreso.setVisibility(View.GONE);
        vista.lista.setVisibility(View.GONE);
        vista.bloqueVacio.setVisibility(View.VISIBLE);
        vista.tituloVacio.setText(R.string.titulo_no_se_pudo_cargar);
        vista.textoVacio.setText(mensaje);
        vista.botonPedirTurno.setVisibility(View.GONE);
    }
}
