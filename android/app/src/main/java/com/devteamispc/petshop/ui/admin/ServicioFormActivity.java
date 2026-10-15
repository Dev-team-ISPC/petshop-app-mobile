package com.devteamispc.petshop.ui.admin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.Servicio;
import com.devteamispc.petshop.databinding.ActivityServicioFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.util.Validaciones;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ServicioFormActivity — CRUD 2: Create, Update y Delete de servicios.
 *
 * Endpoints: POST /servicios/ · PATCH /servicios/{id}/ · DELETE /servicios/{id}/
 *
 * Sin EXTRA_SERVICIO_ID es un alta; con él, una edición con los datos que
 * llegan por extras desde la lista.
 * Un servicio con turnos registrados no se puede eliminar (la base lo protege):
 * en ese caso se ofrece desactivarlo.
 */
public class ServicioFormActivity extends BaseActivity {

    public static final String EXTRA_SERVICIO_ID = "servicio_id";
    public static final String EXTRA_NOMBRE = "servicio_nombre";
    public static final String EXTRA_DESCRIPCION = "servicio_descripcion";
    public static final String EXTRA_DURACION = "servicio_duracion";
    public static final String EXTRA_ACTIVO = "servicio_activo";

    private static final int DURACION_POR_DEFECTO = 30;

    private ActivityServicioFormBinding vista;
    private int servicioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esAdmin()) {
            aviso(getString(R.string.msg_solo_admin));
            finish();
            return;
        }

        vista = ActivityServicioFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        servicioId = getIntent().getIntExtra(EXTRA_SERVICIO_ID, 0);
        boolean esAlta = servicioId == 0;
        setTitle(esAlta ? R.string.titulo_servicio_nuevo : R.string.titulo_servicio_editar);

        if (esAlta) {
            vista.campoDuracion.setText(String.valueOf(DURACION_POR_DEFECTO));
            vista.campoActivo.setChecked(true);
        } else {
            vista.campoNombre.setText(getIntent().getStringExtra(EXTRA_NOMBRE));
            vista.campoDescripcion.setText(getIntent().getStringExtra(EXTRA_DESCRIPCION));
            vista.campoDuracion.setText(String.valueOf(
                    getIntent().getIntExtra(EXTRA_DURACION, DURACION_POR_DEFECTO)));
            vista.campoActivo.setChecked(getIntent().getBooleanExtra(EXTRA_ACTIVO, true));
        }

        vista.bloqueEliminar.setVisibility(esAlta ? View.GONE : View.VISIBLE);
        vista.botonGuardar.setOnClickListener(v -> guardar());
        vista.botonEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    // -------------------------------------------------------------- guardar

    private void guardar() {
        String nombre = vista.campoNombre.getText().toString().trim();
        Integer duracion = Validaciones.duracion(vista.campoDuracion.getText().toString());

        vista.campoNombre.setError(null);
        vista.campoDuracion.setError(null);
        boolean ok = true;
        if (!Validaciones.obligatorio(nombre)) {
            vista.campoNombre.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        if (!Validaciones.duracionValida(duracion)) {
            vista.campoDuracion.setError(getString(R.string.error_duracion,
                    Validaciones.DURACION_MINIMA, Validaciones.DURACION_MAXIMA));
            ok = false;
        }
        if (!ok) return;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("descripcion", vista.campoDescripcion.getText().toString().trim());
        cuerpo.put("duracion_minutos", duracion);
        cuerpo.put("activo", vista.campoActivo.isChecked());

        Call<Servicio> llamada = servicioId == 0
                ? ApiClient.getApi().crearServicio(cuerpo)
                : ApiClient.getApi().editarServicio(servicioId, cuerpo);

        cargando(true);
        llamada.enqueue(new Callback<Servicio>() {
            @Override
            public void onResponse(@NonNull Call<Servicio> call, @NonNull Response<Servicio> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(servicioId == 0 ? R.string.msg_servicio_creado : R.string.msg_cambios_guardados));
                    finish();
                } else {
                    ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                    if (errores.campo("nombre") != null) vista.campoNombre.setError(errores.campo("nombre"));
                    if (errores.campo("duracion_minutos") != null) vista.campoDuracion.setError(errores.campo("duracion_minutos"));
                    if (errores.general() != null) aviso(errores.general());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Servicio> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    // ------------------------------------------------------------- eliminar

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar_servicio)
                .setMessage(R.string.msg_eliminar_servicio)
                .setPositiveButton(R.string.accion_eliminar, (d, b) -> eliminar())
                .setNegativeButton(R.string.accion_volver, null)
                .show();
    }

    private void eliminar() {
        cargando(true);
        ApiClient.getApi().eliminarServicio(servicioId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.msg_servicio_eliminado));
                    finish();
                } else if (respuesta.code() == 409 || respuesta.code() >= 500) {
                    // Tiene turnos: la base no permite borrarlo (on_delete=PROTECT).
                    ofrecerDesactivar();
                } else {
                    aviso(ApiError.mensaje(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                cargando(false);
                aviso(ApiError.sinConexion());
            }
        });
    }

    private void ofrecerDesactivar() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_no_se_puede_eliminar)
                .setMessage(R.string.msg_servicio_con_turnos)
                .setPositiveButton(R.string.accion_desactivar, (d, b) -> {
                    vista.campoActivo.setChecked(false);
                    guardar();
                })
                .setNegativeButton(R.string.accion_volver, null)
                .show();
    }

    private void cargando(boolean si) {
        vista.progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        vista.botonGuardar.setEnabled(!si);
        vista.botonEliminar.setEnabled(!si);
    }
}
