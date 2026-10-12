package com.devteamispc.petshop.ui.vacunacion;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.api.ApiError;
import com.devteamispc.petshop.data.api.ErroresDeCampo;
import com.devteamispc.petshop.data.model.Vacuna;
import com.devteamispc.petshop.databinding.ActivityVacunaFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;
import com.devteamispc.petshop.util.ReglasVacunacion;
import com.devteamispc.petshop.util.Validaciones;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 19 · VacunaFormActivity — alta de una vacuna en el catálogo. Sólo veterinario.
 *
 * Endpoint: POST /vacunas/
 *
 * La frecuencia se carga en meses (como en el mockup) y se guarda como texto
 * que el formulario de vacunación vuelve a entender ("anual", "cada 6 meses"),
 * para sugerir la próxima dosis.
 */
public class VacunaFormActivity extends BaseActivity {

    private static final int MESES_MAXIMO = 120;

    private ActivityVacunaFormBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;
        if (!sesion.esVeterinario()) {
            aviso(getString(R.string.msg_solo_veterinario_catalogo));
            finish();
            return;
        }

        vista = ActivityVacunaFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        vista.campoFrecuencia.setText("12");
        vista.botonGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String nombre = vista.campoNombre.getText().toString().trim();
        Integer meses = Validaciones.duracion(vista.campoFrecuencia.getText().toString());

        vista.campoNombre.setError(null);
        vista.campoFrecuencia.setError(null);
        boolean ok = true;
        if (!Validaciones.obligatorio(nombre)) {
            vista.campoNombre.setError(getString(R.string.error_obligatorio));
            ok = false;
        }
        if (meses == null || meses < 1 || meses > MESES_MAXIMO) {
            vista.campoFrecuencia.setError(getString(R.string.error_frecuencia, MESES_MAXIMO));
            ok = false;
        }
        if (!ok) return;

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("nombre", nombre);
        cuerpo.put("descripcion", vista.campoDescripcion.getText().toString().trim());
        cuerpo.put("frecuencia", ReglasVacunacion.frecuenciaDesdeMeses(meses));

        cargando(true);
        ApiClient.getApi().crearVacuna(cuerpo).enqueue(new Callback<Vacuna>() {
            @Override
            public void onResponse(@NonNull Call<Vacuna> call, @NonNull Response<Vacuna> respuesta) {
                cargando(false);
                if (manejarErrorComun(respuesta)) return;
                if (respuesta.isSuccessful()) {
                    aviso(getString(R.string.msg_vacuna_creada));
                    finish();
                } else {
                    ErroresDeCampo errores = ErroresDeCampo.de(respuesta);
                    // El nombre es único en el catálogo: el error viene en "nombre".
                    if (errores.campo("nombre") != null) vista.campoNombre.setError(errores.campo("nombre"));
                    if (errores.campo("frecuencia") != null) vista.campoFrecuencia.setError(errores.campo("frecuencia"));
                    if (errores.general() != null) aviso(errores.general());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Vacuna> call, @NonNull Throwable t) {
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
