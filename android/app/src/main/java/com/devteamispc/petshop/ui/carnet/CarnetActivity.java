package com.devteamispc.petshop.ui.carnet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.model.CambioEstado;
import com.devteamispc.petshop.data.model.Mascota;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Turno;
import com.devteamispc.petshop.data.model.Vacunacion;
import com.devteamispc.petshop.databinding.ActivityCarnetBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.mascota.MascotaFormActivity;
import com.devteamispc.petshop.ui.turno.TurnoFormActivity;
import com.devteamispc.petshop.ui.vacunacion.VacunacionFormActivity;
import com.devteamispc.petshop.util.Barra;
import com.devteamispc.petshop.util.Fechas;
import com.google.android.material.tabs.TabLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 6 · CarnetActivity
 *
 * Endpoints: GET /mascotas/{id}/vacunaciones/  ·  GET /mascotas/{id}/turnos/
 *
 * Libreta sanitaria de una mascota, con dos pestañas: Vacunas y Turnos.
 * Recibe la mascota por EXTRA_MASCOTA_ID (traspaso de información entre
 * Activities) y la reenvía a los formularios de vacunación y de turno.
 *
 * Las dos listas se piden juntas al abrir la pantalla y cada pestaña recuerda
 * su propio estado (cargando, error o datos), así cambiar de pestaña es instantáneo.
 *
 * El botón + cambia según la pestaña Y el rol:
 *   Vacunas -> sólo veterinario (registrar vacunación; al cliente la API le da 403)
 *   Turnos  -> sólo cliente (solicitar turno)
 * El administrador sólo consulta: no ve botones de alta.
 *
 * Arriba, el subtítulo con especie, raza y peso (o el dueño, para el personal),
 * y en el menú "Editar mascota" para el dueño y el veterinario.
 * En la pestaña Turnos, el cliente puede cancelar los suyos (no se borran:
 * quedan registrados como cancelados).
 */
public class CarnetActivity extends BaseActivity {

    /** Traspaso de información entre Activities: lo que evalúa el enunciado. */
    public static final String EXTRA_MASCOTA_ID = "mascota_id";
    public static final String EXTRA_MASCOTA_NOMBRE = "mascota_nombre";

    private static final int PESTANA_VACUNAS = 0;

    private ActivityCarnetBinding vista;
    private int mascotaId;
    private String mascotaNombre;

    private final VacunacionAdapter adapterVacunas = new VacunacionAdapter();
    private TurnoAdapter adapterTurnos;
    private final EstadoSeccion vacunas = new EstadoSeccion();
    private final EstadoSeccion turnos = new EstadoSeccion();

    /** Lo que sabe cada pestaña de su propia carga. */
    private static class EstadoSeccion {
        boolean cargando;
        String error;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityCarnetBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        mascotaId = getIntent().getIntExtra(EXTRA_MASCOTA_ID, 0);
        mascotaNombre = getIntent().getStringExtra(EXTRA_MASCOTA_NOMBRE);

        if (mascotaId == 0) {
            aviso("No se recibió la mascota.");
            finish();
            return;
        }

        setTitle(getString(R.string.titulo_carnet) + ": " + mascotaNombre);

        adapterTurnos = new TurnoAdapter(sesion.esCliente(), this::confirmarCancelacion);
        vista.lista.setLayoutManager(new LinearLayoutManager(this));
        vista.refrescar.setOnRefreshListener(this::cargarTodo);
        vista.botonAgregar.setOnClickListener(v -> agregar());
        vista.pestanas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { mostrarPestana(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
        mostrarPestana();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de un formulario, la vacuna o el turno nuevo aparece solo.
        cargarTodo();
    }

    private boolean enVacunas() {
        return vista.pestanas.getSelectedTabPosition() == PESTANA_VACUNAS;
    }

    // ---------------------------------------------------------------- carga

    private void cargarTodo() {
        cargarMascota();
        cargarVacunas();
        cargarTurnos();
    }

    /** Datos de la mascota para el título y el subtítulo (pudo cambiar al editarla). */
    private void cargarMascota() {
        ApiClient.getApi().mascota(mascotaId).enqueue(new Callback<Mascota>() {
            @Override
            public void onResponse(@NonNull Call<Mascota> call, @NonNull Response<Mascota> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (!respuesta.isSuccessful() || respuesta.body() == null) return;
                Mascota m = respuesta.body();
                mascotaNombre = m.getNombre();
                setTitle(getString(R.string.titulo_carnet) + ": " + mascotaNombre);
                String subtitulo = m.getEspecieDisplay() + " · " + m.getRaza();
                if (sesion.esCliente()) {
                    if (m.getPeso() != null) subtitulo += " · " + m.getPeso().replace('.', ',') + " kg";
                } else if (m.getDuenoNombre() != null) {
                    subtitulo += " · " + getString(R.string.carnet_dueno, m.getDuenoNombre());
                }
                Barra.subtitulo(CarnetActivity.this, subtitulo);
            }

            @Override
            public void onFailure(@NonNull Call<Mascota> call, @NonNull Throwable t) {
                // Sin subtítulo: el resto del carnet avisa si no hay conexión.
            }
        });
    }

    private void cargarVacunas() {
        vacunas.cargando = true;
        vacunas.error = null;
        pintarEstado();
        ApiClient.getApi().vacunacionesDeMascota(mascotaId).enqueue(new Callback<Pagina<Vacunacion>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Vacunacion>> call,
                                   @NonNull Response<Pagina<Vacunacion>> respuesta) {
                vacunas.cargando = false;
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    adapterVacunas.actualizar(respuesta.body().getResults());
                } else {
                    vacunas.error = ApiError.mensaje(respuesta);
                }
                terminarCarga();
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Vacunacion>> call, @NonNull Throwable t) {
                vacunas.cargando = false;
                vacunas.error = ApiError.sinConexion();
                terminarCarga();
            }
        });
    }

    private void cargarTurnos() {
        turnos.cargando = true;
        turnos.error = null;
        pintarEstado();
        ApiClient.getApi().turnosDeMascota(mascotaId).enqueue(new Callback<Pagina<Turno>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Turno>> call,
                                   @NonNull Response<Pagina<Turno>> respuesta) {
                turnos.cargando = false;
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    adapterTurnos.actualizar(respuesta.body().getResults());
                } else {
                    turnos.error = ApiError.mensaje(respuesta);
                }
                terminarCarga();
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Turno>> call, @NonNull Throwable t) {
                turnos.cargando = false;
                turnos.error = ApiError.sinConexion();
                terminarCarga();
            }
        });
    }

    private void terminarCarga() {
        if (!vacunas.cargando && !turnos.cargando) {
            vista.refrescar.setRefreshing(false);
        }
        pintarEstado();
    }

    // ---------------------------------------------------------- presentación

    /** Cambia la lista y el botón + según la pestaña elegida. */
    private void mostrarPestana() {
        RecyclerView.Adapter<?> adapter = enVacunas() ? adapterVacunas : adapterTurnos;
        if (vista.lista.getAdapter() != adapter) {
            vista.lista.setAdapter(adapter);
        }

        boolean visible;
        if (enVacunas()) {
            visible = sesion.esVeterinario();
            vista.botonAgregar.setContentDescription(getString(R.string.desc_nueva_vacuna));
        } else {
            visible = sesion.esCliente();
            vista.botonAgregar.setContentDescription(getString(R.string.desc_nuevo_turno));
        }
        vista.botonAgregar.setVisibility(visible ? View.VISIBLE : View.GONE);

        pintarEstado();
    }

    /** Muestra cargando, error, vacío o la lista, según la pestaña actual. */
    private void pintarEstado() {
        EstadoSeccion estado = enVacunas() ? vacunas : turnos;
        int cantidad = enVacunas() ? adapterVacunas.getItemCount() : adapterTurnos.getItemCount();

        boolean cargandoSinDatos = estado.cargando && cantidad == 0 && !vista.refrescar.isRefreshing();
        vista.progreso.setVisibility(cargandoSinDatos ? View.VISIBLE : View.GONE);

        if (estado.cargando && cantidad == 0) {
            vista.lista.setVisibility(View.GONE);
            vista.textoVacio.setVisibility(View.GONE);
        } else if (estado.error != null) {
            vista.lista.setVisibility(View.GONE);
            vista.textoVacio.setVisibility(View.VISIBLE);
            vista.textoVacio.setText(estado.error);
        } else if (cantidad == 0) {
            vista.lista.setVisibility(View.GONE);
            vista.textoVacio.setVisibility(View.VISIBLE);
            vista.textoVacio.setText(enVacunas() ? R.string.vacio_vacunas : R.string.vacio_turnos);
        } else {
            vista.lista.setVisibility(View.VISIBLE);
            vista.textoVacio.setVisibility(View.GONE);
        }
    }

    // ------------------------------------------------------------------ menú

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Editar es del dueño o del veterinario; el administrador sólo consulta.
        if (sesion.esCliente() || sesion.esVeterinario()) {
            getMenuInflater().inflate(R.menu.menu_carnet, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuEditarMascota) {
            Intent i = new Intent(this, MascotaFormActivity.class);
            i.putExtra(MascotaFormActivity.EXTRA_MASCOTA_ID, mascotaId);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ------------------------------------------------------- cancelar turno

    private void confirmarCancelacion(Turno t) {
        String detalle = t.getServicioNombre() + " · " + mascotaNombre + "\n"
                + Fechas.fechaHoraAVisible(t.getFecha()) + "\n\n"
                + getString(R.string.msg_cancelar_turno_cliente);
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_cancelar_turno)
                .setMessage(detalle)
                .setPositiveButton(R.string.accion_si_cancelar, (d, b) -> cancelar(t))
                .setNegativeButton(R.string.accion_no_volver, null)
                .show();
    }

    private void cancelar(Turno t) {
        adapterTurnos.marcarOcupado(t.getId());
        ApiClient.getApi().cambiarEstadoTurno(t.getId(), new CambioEstado(Turno.CANCELADO))
                .enqueue(new Callback<Turno>() {
                    @Override
                    public void onResponse(@NonNull Call<Turno> call, @NonNull Response<Turno> respuesta) {
                        adapterTurnos.marcarOcupado(-1);
                        if (manejarErrorComun(respuesta)) return;
                        if (respuesta.isSuccessful()) {
                            aviso(getString(R.string.agenda_cancelado));
                            cargarTurnos();
                        } else {
                            aviso(ApiError.mensaje(respuesta));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Turno> call, @NonNull Throwable t2) {
                        adapterTurnos.marcarOcupado(-1);
                        aviso(ApiError.sinConexion());
                    }
                });
    }

    // --------------------------------------------------------------- acciones

    private void agregar() {
        Class<?> destino = enVacunas() ? VacunacionFormActivity.class : TurnoFormActivity.class;
        Intent i = new Intent(this, destino);
        i.putExtra(EXTRA_MASCOTA_ID, mascotaId);
        i.putExtra(EXTRA_MASCOTA_NOMBRE, mascotaNombre);
        startActivity(i);
    }
}
