package com.devteamispc.petshop.ui.mascota;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.Mascota;
import com.devteamispc.petshop.databinding.ActivityMascotaFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.util.Fechas;
import com.devteamispc.petshop.util.SelectorFecha;
import com.devteamispc.petshop.util.Validaciones;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 5 · MascotaFormActivity — RF03
 *
 * Endpoints: POST /mascotas/ · GET y PATCH /mascotas/{id}/
 *
 * Sin EXTRA_MASCOTA_ID es un alta (sólo el cliente: el servidor le asigna la
 * mascota); con él, una edición (dueño o veterinario, desde el carnet).
 *
 *   - Especie: catálogo cerrado. Se muestra "Perro" pero se envía "perro".
 *   - Peso: admite coma o punto y viaja como texto ("28.50"), como lo serializa DRF.
 *   - Fecha de nacimiento: con el selector de fecha, sin fechas futuras.
 */
public class MascotaFormActivity extends BaseActivity {

    public static final String EXTRA_MASCOTA_ID = "mascota_id";

    private static final String[] ESPECIES_VALOR = { "perro", "gato", "ave", "conejo", "reptil", "otro" };
    private static final String[] ESPECIES_TEXTO = { "Perro", "Gato", "Ave", "Conejo", "Reptil", "Otro" };

    private ActivityMascotaFormBinding vista;
    private int mascotaId;
    /** Fecha elegida, en formato de la API. */
    private String fechaNacimientoApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityMascotaFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        mascotaId = getIntent().getIntExtra(EXTRA_MASCOTA_ID, 0);
        boolean esAlta = mascotaId == 0;
        setTitle(esAlta ? R.string.titulo_mascota_nueva : R.string.titulo_mascota_editar);

        // Dar de alta es del cliente: el veterinario no puede elegir dueño desde la app.
        if (esAlta && !sesion.esCliente()) {
            aviso(getString(R.string.msg_alta_solo_cliente));
            finish();
            return;
        }
        vista.ayudaDueno.setVisibility(esAlta ? View.VISIBLE : View.GONE);

        ArrayAdapter<String> especies = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ESPECIES_TEXTO);
        especies.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vista.campoEspecie.setAdapter(especies);

        vista.campoFecha.setOnClickListener(v -> elegirFecha());
        vista.botonGuardar.setOnClickListener(v -> guardar());

        if (!esAlta) cargar();
    }

    private void elegirFecha() {
        // Sin fechas futuras: no se puede haber nacido mañana.
        SelectorFecha.fecha(this, fechaNacimientoApi, null, System.currentTimeMillis(), (api, visible) -> {
            fechaNacimientoApi = api;
            vista.campoFecha.setText(visible);
            vista.campoFecha.setError(null);
        });
    }

    // ---------------------------------------------------------------- carga

    private void cargar() {
        cargando(true);
        ApiClient.getApi().mascota(mascotaId).enqueue(new Callback<Mascota>() {
            @Override
            public void onResponse(@NonNull Call<Mascota> call, @NonNull Response<Mascota> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    completar(respuesta.body());
                } else {
                    aviso(ApiError.mensaje(respuesta));
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Mascota> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
                finish();
            }
        });
    }

    private void completar(Mascota m) {
        vista.campoNombre.setText(m.getNombre());
        vista.campoRaza.setText(m.getRaza());
        vista.campoPeso.setText(m.getPeso() == null ? "" : m.getPeso().replace('.', ','));
        for (int i = 0; i < ESPECIES_VALOR.length; i++) {
            if (ESPECIES_VALOR[i].equals(m.getEspecie())) vista.campoEspecie.setSelection(i);
        }
        fechaNacimientoApi = m.getFechaNacimiento();
        vista.campoFecha.setText(Fechas.aVisible(fechaNacimientoApi));
    }

    // -------------------------------------------------------------- guardar

    private void guardar() {
        String nombre = vista.campoNombre.getText().toString().trim();
        String raza = vista.campoRaza.getText().toString().trim();
        String peso = Validaciones.pesoParaApi(vista.campoPeso.getText().toString());

        vista.campoNombre.setError(null);
        vista.campoRaza.setError(null);
        vista.campoPeso.setError(null);
        vista.campoFecha.setError(null);
        boolean ok = true;
        if (!Validaciones.obligatorio(nombre)) {
            vista.campoNombre.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        if (!Validaciones.obligatorio(raza)) {
            vista.campoRaza.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        if (peso == null) {
            vista.campoPeso.setError(getString(R.string.error_peso));
            ok = false;
        }
        if (fechaNacimientoApi == null) {
            vista.campoFecha.setError(getString(R.string.error_fecha_nacimiento));
            ok = false;
        }
        if (!ok) return;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("especie", ESPECIES_VALOR[vista.campoEspecie.getSelectedItemPosition()]);
        cuerpo.put("raza", raza);
        cuerpo.put("peso", peso);
        cuerpo.put("fecha_nacimiento", fechaNacimientoApi);

        Call<Mascota> llamada = mascotaId == 0
                ? ApiClient.getApi().crearMascota(new Mascota(nombre, (String) cuerpo.get("especie"), raza, peso, fechaNacimientoApi))
                : ApiClient.getApi().editarMascota(mascotaId, cuerpo);

        cargando(true);
        llamada.enqueue(new Callback<Mascota>() {
            @Override
            public void onResponse(@NonNull Call<Mascota> call, @NonNull Response<Mascota> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(mascotaId == 0 ? R.string.msg_mascota_creada : R.string.msg_cambios_guardados));
                    finish();
                } else {
                    ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                    if (errores.campo("nombre") != null) vista.campoNombre.setError(errores.campo("nombre"));
                    if (errores.campo("raza") != null) vista.campoRaza.setError(errores.campo("raza"));
                    if (errores.campo("peso") != null) vista.campoPeso.setError(errores.campo("peso"));
                    if (errores.campo("fecha_nacimiento") != null) vista.campoFecha.setError(errores.campo("fecha_nacimiento"));
                    if (errores.general() != null) aviso(errores.general());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Mascota> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void cargando(boolean si) {
        vista.progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        vista.botonGuardar.setEnabled(!si);
    }
}
