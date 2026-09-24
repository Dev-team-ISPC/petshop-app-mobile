package com.devteamispc.petshop.ui.turno;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityTurnoFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 7 · TurnoFormActivity
 *
 * Endpoints que consume: POST /turnos/  ·  GET /servicios/?activo=true
 *
 * El servicio sale del catálogo: cargar el Spinner con GET /servicios/?activo=true
 * y page_size alto para traerlos todos de una.
 * La fecha y hora se arman con Fechas.aApiFechaHora(): la API espera ISO 8601 UTC.
 * El turno se crea siempre como pendiente; el estado no se manda.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_turno_form.xml.
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
public class TurnoFormActivity extends BaseActivity {

    private ActivityTurnoFormBinding vista;
    private int mascotaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityTurnoFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // Llega desde el carnet, con la mascota ya elegida.
        mascotaId = getIntent().getIntExtra(
                com.devteamispc.petshop.ui.carnet.CarnetActivity.EXTRA_MASCOTA_ID, 0);

        // TODO: implementar la pantalla
    }
}
