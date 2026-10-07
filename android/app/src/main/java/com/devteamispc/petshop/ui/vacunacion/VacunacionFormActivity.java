package com.devteamispc.petshop.ui.vacunacion;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityVacunacionFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 8 · VacunacionFormActivity
 *
 * Endpoints que consume: POST /vacunaciones/  ·  GET /vacunas/
 *
 * SÓLO VETERINARIO. Al admin la API le devuelve 403, a propósito.
 * Se llega desde el carnet, con la mascota ya elegida: recibe EXTRA_MASCOTA_ID.
 * El veterinario queda asignado solo, no hace falta mandarlo.
 * Fechas en yyyy-MM-dd; proxima_dosis debe ser posterior a fecha_aplicacion.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_vacunacion_form.xml.
 * El manifest, los colores, los estilos y la capa de red ya están hechos:
 * si los modificás, chocás con el resto del equipo en el merge.
 *
 * Patrones a copiar:
 *   listado con filtros  -> ui/mascota/MascotasActivity.java
 *   formulario con POST  -> ui/auth/LoginActivity.java
 *
 * Llamadas: ApiClient.getApi().loQueNecesites(...).enqueue(...)
 * Errores:  ApiError.mensaje(respuesta) y manejarErrorComun(respuesta)
 */
public class VacunacionFormActivity extends BaseActivity {

    private ActivityVacunacionFormBinding vista;
    private int mascotaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityVacunacionFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // Llega desde el carnet, con la mascota ya elegida.
        mascotaId = getIntent().getIntExtra(
                com.devteamispc.petshop.ui.carnet.CarnetActivity.EXTRA_MASCOTA_ID, 0);

        // TODO: implementar la pantalla
    }
}
