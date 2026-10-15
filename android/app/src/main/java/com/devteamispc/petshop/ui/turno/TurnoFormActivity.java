package com.devteamispc.petshop.ui.turno;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.Mascota;
import com.devteamispc.petshop.data.model.NuevoTurno;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Servicio;
import com.devteamispc.petshop.data.model.Turno;
import com.devteamispc.petshop.databinding.ActivityTurnoFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.carnet.CarnetActivity;
import com.devteamispc.petshop.util.Barra;
import com.devteamispc.petshop.util.Fechas;
import com.devteamispc.petshop.util.SelectorFecha;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 7 · TurnoFormActivity — RF07
 *
 * Endpoints: POST /turnos/ · GET /servicios/?activo=true · GET /mascotas/
 *
 * Desde el carnet llega la mascota ya elegida (EXTRA_MASCOTA_ID); sin ella,
 * se elige de la lista de mascotas del cliente.
 *
 *   - Sólo se ofrecen servicios activos, con su duración estimada.
 *   - Fecha desde hoy en adelante; la hora tiene que quedar en el futuro.
 *   - La fecha y hora viajan en ISO 8601 UTC (Fechas.aApiFechaHora).
 *   - El turno se crea pendiente: no se manda estado. Lo confirma un veterinario.
 */
public class TurnoFormActivity extends BaseActivity {

    private ActivityTurnoFormBinding vista;
    private int mascotaId;

    private final List<Servicio> servicios = new ArrayList<>();
    private final List<Mascota> mascotas = new ArrayList<>();

    private String fechaApi;
    private int anio, mes, dia;
    private Integer hora, minuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityTurnoFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        mascotaId = getIntent().getIntExtra(CarnetActivity.EXTRA_MASCOTA_ID, 0);
        String mascotaNombre = getIntent().getStringExtra(CarnetActivity.EXTRA_MASCOTA_NOMBRE);

        if (mascotaId != 0) {
            // La mascota viene elegida: se muestra como dato, sin selector.
            vista.bloqueMascota.setVisibility(View.GONE);
            Barra.subtitulo(this, mascotaNombre);
        } else {
            vista.bloqueMascota.setVisibility(View.VISIBLE);
            cargarMascotas();
        }

        vista.campoServicio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View v, int posicion, long id) {
                mostrarDuracion(posicion);
            }

            @Override public void onNothingSelected(AdapterView<?> padre) { }
        });
        vista.campoFecha.setOnClickListener(v -> elegirFecha());
        vista.campoHora.setOnClickListener(v -> elegirHora());
        vista.botonSolicitar.setOnClickListener(v -> solicitar());

        cargarServicios();
    }

    // ------------------------------------------------------------- catálogos

    private void cargarServicios() {
        Map<String, String> filtros = new HashMap<>();
        filtros.put("activo", "true");
        filtros.put("ordering", "nombre");
        filtros.put("page_size", "100");
        ApiClient.getApi().servicios(filtros).enqueue(new Callback<Pagina<Servicio>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Servicio>> call, @NonNull Response<Pagina<Servicio>> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    servicios.clear();
                    servicios.addAll(respuesta.body().getResults());
                    ArrayAdapter<Servicio> adapter = new ArrayAdapter<>(TurnoFormActivity.this,
                            android.R.layout.simple_spinner_item, servicios);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    vista.campoServicio.setAdapter(adapter);
                    if (servicios.isEmpty()) aviso(getString(R.string.msg_sin_servicios));
                } else {
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Servicio>> call, @NonNull Throwable t) {
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void cargarMascotas() {
        Map<String, String> filtros = new HashMap<>();
        filtros.put("ordering", "nombre");
        filtros.put("page_size", "100");
        ApiClient.getApi().mascotas(filtros).enqueue(new Callback<Pagina<Mascota>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Mascota>> call, @NonNull Response<Pagina<Mascota>> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    mascotas.clear();
                    mascotas.addAll(respuesta.body().getResults());
                    List<String> textos = new ArrayList<>();
                    for (Mascota m : mascotas) textos.add(m.getNombre() + " · " + m.getRaza());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(TurnoFormActivity.this,
                            android.R.layout.simple_spinner_item, textos);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    vista.campoMascota.setAdapter(adapter);
                    if (mascotas.isEmpty()) aviso(getString(R.string.msg_sin_mascotas_turno));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Mascota>> call, @NonNull Throwable t) {
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void mostrarDuracion(int posicion) {
        if (posicion < 0 || posicion >= servicios.size()) {
            vista.ayudaDuracion.setVisibility(View.GONE);
            return;
        }
        vista.ayudaDuracion.setVisibility(View.VISIBLE);
        vista.ayudaDuracion.setText(getString(R.string.duracion_estimada, servicios.get(posicion).getDuracionMinutos()));
    }

    // ------------------------------------------------------------ fecha/hora

    private void elegirFecha() {
        Calendar hoy = Calendar.getInstance();
        SelectorFecha.fecha(this, fechaApi, hoy.getTimeInMillis() - 1000, null, (api, visible) -> {
            fechaApi = api;
            String[] p = api.split("-");
            anio = Integer.parseInt(p[0]);
            mes = Integer.parseInt(p[1]) - 1;
            dia = Integer.parseInt(p[2]);
            vista.campoFecha.setText(visible);
            vista.campoFecha.setError(null);
        });
    }

    private void elegirHora() {
        int h = hora != null ? hora : 10;
        int m = minuto != null ? minuto : 0;
        SelectorFecha.hora(this, h, m, (hh, mm) -> {
            hora = hh;
            minuto = mm;
            vista.campoHora.setText(String.format(Locale.US, "%02d:%02d", hh, mm));
            vista.campoHora.setError(null);
        });
    }

    /** Milisegundos del turno elegido, en hora local. */
    private long momentoElegido() {
        Calendar c = Calendar.getInstance();
        c.set(anio, mes, dia, hora, minuto, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // ------------------------------------------------------------- solicitar

    private void solicitar() {
        vista.campoFecha.setError(null);
        vista.campoHora.setError(null);
        vista.textoError.setVisibility(View.GONE);

        int idMascota = mascotaId;
        if (idMascota == 0) {
            int pos = vista.campoMascota.getSelectedItemPosition();
            if (pos < 0 || pos >= mascotas.size()) {
                mostrarError(getString(R.string.msg_sin_mascotas_turno));
                return;
            }
            idMascota = mascotas.get(pos).getId();
        }
        int posServicio = vista.campoServicio.getSelectedItemPosition();
        if (posServicio < 0 || posServicio >= servicios.size()) {
            mostrarError(getString(R.string.msg_sin_servicios));
            return;
        }
        if (fechaApi == null) {
            vista.campoFecha.setError(getString(R.string.error_fecha_turno));
            return;
        }
        if (hora == null) {
            vista.campoHora.setError(getString(R.string.error_hora_turno));
            return;
        }
        if (momentoElegido() <= System.currentTimeMillis()) {
            vista.campoHora.setError(getString(R.string.error_turno_pasado));
            return;
        }

        String observaciones = vista.campoObservaciones.getText().toString().trim();
        NuevoTurno pedido = new NuevoTurno(idMascota, servicios.get(posServicio).getId(),
                Fechas.aApiFechaHora(anio, mes, dia, hora, minuto), observaciones);

        cargando(true);
        ApiClient.getApi().crearTurno(pedido).enqueue(new Callback<Turno>() {
            @Override
            public void onResponse(@NonNull Call<Turno> call, @NonNull Response<Turno> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.msg_turno_solicitado));
                    finish();
                } else {
                    ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                    if (errores.campo("fecha") != null) vista.campoFecha.setError(errores.campo("fecha"));
                    StringBuilder resto = new StringBuilder();
                    for (String campo : new String[]{"mascota", "servicio", "observaciones"}) {
                        if (errores.campo(campo) != null) resto.append(errores.campo(campo)).append('\n');
                    }
                    if (errores.general() != null) resto.append(errores.general());
                    if (resto.length() > 0) mostrarError(resto.toString().trim());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Turno> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void cargando(boolean si) {
        vista.progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        vista.botonSolicitar.setEnabled(!si);
    }

    private void mostrarError(String mensaje) {
        vista.textoError.setText(mensaje);
        vista.textoError.setVisibility(View.VISIBLE);
    }
}
