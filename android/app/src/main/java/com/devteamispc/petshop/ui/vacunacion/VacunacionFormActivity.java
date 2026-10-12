package com.devteamispc.petshop.ui.vacunacion;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.NuevaVacunacion;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Usuario;
import com.devteamispc.petshop.data.model.Vacuna;
import com.devteamispc.petshop.data.model.Vacunacion;
import com.devteamispc.petshop.databinding.ActivityVacunacionFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.ui.carnet.CarnetActivity;
import com.devteamispc.petshop.util.Barra;
import com.devteamispc.petshop.util.Fechas;
import com.devteamispc.petshop.util.ReglasVacunacion;
import com.devteamispc.petshop.util.SelectorFecha;
import com.devteamispc.petshop.util.Validaciones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 8 · VacunacionFormActivity — RF05
 *
 * Endpoints: POST /vacunaciones/ · GET /vacunas/
 *
 * SÓLO VETERINARIO: al admin y al cliente la API les devuelve 403, y la
 * pantalla se cierra si la abre otro rol. Se llega desde el carnet, con la
 * mascota ya elegida (EXTRA_MASCOTA_ID).
 *
 *   - La fecha de aplicación arranca en hoy y no puede ser futura.
 *   - La próxima dosis se sugiere según la frecuencia de la vacuna
 *     (ReglasVacunacion) y tiene que ser posterior a la aplicación.
 *   - El veterinario queda asignado solo: se muestra, no se elige.
 */
public class VacunacionFormActivity extends BaseActivity {

    private ActivityVacunacionFormBinding vista;
    private int mascotaId;

    private final List<Vacuna> vacunas = new ArrayList<>();
    private String fechaAplicacionApi;
    private String proximaDosisApi;
    /** Si el veterinario eligió la próxima dosis a mano, no se pisa con la sugerencia. */
    private boolean proximaElegidaAMano = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esVeterinario()) {
            aviso(getString(R.string.msg_solo_veterinario_vacunas));
            finish();
            return;
        }

        vista = ActivityVacunacionFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        mascotaId = getIntent().getIntExtra(CarnetActivity.EXTRA_MASCOTA_ID, 0);
        if (mascotaId == 0) {
            aviso("No se recibió la mascota.");
            finish();
            return;
        }
        Barra.subtitulo(this, getIntent().getStringExtra(CarnetActivity.EXTRA_MASCOTA_NOMBRE));

        Usuario yo = sesion.getUsuario();
        vista.campoVeterinario.setText(yo == null ? "" : yo.getNombre());

        fechaAplicacionApi = SelectorFecha.hoyApi();
        vista.campoAplicacion.setText(Fechas.aVisible(fechaAplicacionApi));

        vista.campoVacuna.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View v, int posicion, long id) {
                alCambiarVacuna(posicion);
            }

            @Override public void onNothingSelected(AdapterView<?> padre) { }
        });
        vista.campoAplicacion.setOnClickListener(v -> elegirAplicacion());
        vista.campoProxima.setOnClickListener(v -> elegirProxima());
        vista.botonGuardar.setOnClickListener(v -> guardar());

        cargarVacunas();
    }

    private void cargarVacunas() {
        Map<String, String> filtros = new HashMap<>();
        filtros.put("ordering", "nombre");
        filtros.put("page_size", "100");
        ApiClient.getApi().vacunas(filtros).enqueue(new Callback<Pagina<Vacuna>>() {
            @Override
            public void onResponse(@NonNull Call<Pagina<Vacuna>> call, @NonNull Response<Pagina<Vacuna>> respuesta) {
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    vacunas.clear();
                    vacunas.addAll(respuesta.body().getResults());
                    ArrayAdapter<Vacuna> adapter = new ArrayAdapter<>(VacunacionFormActivity.this,
                            android.R.layout.simple_spinner_item, vacunas);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    vista.campoVacuna.setAdapter(adapter);
                } else {
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Pagina<Vacuna>> call, @NonNull Throwable t) {
                aviso(ApiError.sinConexion());
            }
        });
    }

    private Vacuna vacunaElegida() {
        int pos = vista.campoVacuna.getSelectedItemPosition();
        return pos >= 0 && pos < vacunas.size() ? vacunas.get(pos) : null;
    }

    private void alCambiarVacuna(int posicion) {
        Vacuna v = posicion >= 0 && posicion < vacunas.size() ? vacunas.get(posicion) : null;
        String frecuencia = v == null ? null : v.getFrecuencia();
        boolean hay = frecuencia != null && !frecuencia.isEmpty();
        vista.ayudaFrecuencia.setVisibility(hay ? View.VISIBLE : View.GONE);
        if (hay) vista.ayudaFrecuencia.setText(getString(R.string.frecuencia_sugerida, frecuencia));
        sugerirProxima();
    }

    /** Completa la próxima dosis según la frecuencia, salvo que se haya elegido a mano. */
    private void sugerirProxima() {
        if (proximaElegidaAMano) return;
        Vacuna v = vacunaElegida();
        String sugerida = v == null ? null : ReglasVacunacion.sugerirProximaDosis(fechaAplicacionApi, v.getFrecuencia());
        proximaDosisApi = sugerida;
        vista.campoProxima.setText(sugerida == null ? "" : Fechas.aVisible(sugerida));
    }

    private void elegirAplicacion() {
        SelectorFecha.fecha(this, fechaAplicacionApi, null, System.currentTimeMillis(), (api, visible) -> {
            fechaAplicacionApi = api;
            vista.campoAplicacion.setText(visible);
            vista.campoAplicacion.setError(null);
            sugerirProxima();
        });
    }

    private void elegirProxima() {
        String inicial = proximaDosisApi != null ? proximaDosisApi : fechaAplicacionApi;
        SelectorFecha.fecha(this, inicial, System.currentTimeMillis(), null, (api, visible) -> {
            proximaDosisApi = api;
            proximaElegidaAMano = true;
            vista.campoProxima.setText(visible);
            vista.campoProxima.setError(null);
        });
    }

    private void guardar() {
        vista.campoProxima.setError(null);
        vista.textoError.setVisibility(View.GONE);

        Vacuna v = vacunaElegida();
        if (v == null) {
            mostrarError(getString(R.string.msg_sin_vacunas));
            return;
        }
        if (proximaDosisApi != null && !Validaciones.esPosterior(proximaDosisApi, fechaAplicacionApi)) {
            vista.campoProxima.setError(getString(R.string.error_proxima_dosis));
            return;
        }

        cargando(true);
        NuevaVacunacion pedido = new NuevaVacunacion(mascotaId, v.getId(), fechaAplicacionApi, proximaDosisApi);
        ApiClient.getApi().crearVacunacion(pedido).enqueue(new Callback<Vacunacion>() {
            @Override
            public void onResponse(@NonNull Call<Vacunacion> call, @NonNull Response<Vacunacion> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.msg_vacunacion_guardada));
                    finish();
                } else {
                    ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                    if (errores.campo("fecha_aplicacion") != null) vista.campoAplicacion.setError(errores.campo("fecha_aplicacion"));
                    if (errores.campo("proxima_dosis") != null) vista.campoProxima.setError(errores.campo("proxima_dosis"));
                    if (errores.general() != null) mostrarError(errores.general());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Vacunacion> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void cargando(boolean si) {
        vista.progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        vista.botonGuardar.setEnabled(!si);
    }

    private void mostrarError(String mensaje) {
        vista.textoError.setText(mensaje);
        vista.textoError.setVisibility(View.VISIBLE);
    }
}
