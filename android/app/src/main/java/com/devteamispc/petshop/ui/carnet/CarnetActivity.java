package com.devteamispc.petshop.ui.carnet;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityCarnetBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 6 · CarnetActivity
 *
 * Endpoints que consume: GET /mascotas/{id}/vacunaciones/  ·  GET /mascotas/{id}/turnos/
 *
 * Recibe el id de la mascota por EXTRA_MASCOTA_ID. Dos pestañas: Vacunas y Turnos.
 * El botón + cambia según pestaña Y rol:
 *   pestaña Vacunas  -> sólo veterinario (el cliente recibiría 403)
 *   pestaña Turnos   -> el cliente pide turno
 * En cada vacuna mostrar el veterinario que la aplicó y el badge de días,
 * que viene calculado en dias_para_proxima_dosis (negativo si está vencida).
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_carnet.xml.
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
public class CarnetActivity extends BaseActivity {

    /** Traspaso de información entre Activities: lo que evalúa el enunciado. */
    public static final String EXTRA_MASCOTA_ID = "mascota_id";
    public static final String EXTRA_MASCOTA_NOMBRE = "mascota_nombre";

    private ActivityCarnetBinding vista;
    private int mascotaId;
    private String mascotaNombre;

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
        setTitle("Carnet: " + mascotaNombre);

        // TODO: implementar la pantalla
    }
}
