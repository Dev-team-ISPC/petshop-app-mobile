package com.devteamispc.petshop.ui.home;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityHomeBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 3 · HomeActivity
 *
 * Endpoints que consume: GET /resumen/
 *
 * UNA sola Activity para los tres roles: se piden los datos a /resumen/ y se
 * muestran fichas distintas según sesion.getRol(). Esto es lo que demuestra el
 * requisito de panel diferenciado: mismo código, tres resultados.
 * cliente: total_mascotas, turnos_pendientes y proxima_dosis destacada.
 * veterinario: turnos_pendientes y turnos_hoy.
 * admin: total_usuarios, total_mascotas, total_turnos, consultas_sin_leer.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_home.xml.
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
public class HomeActivity extends BaseActivity {

    private ActivityHomeBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());

        vista.botonMascotas.setOnClickListener(v ->
                startActivity(new android.content.Intent(this,
                        com.devteamispc.petshop.ui.mascota.MascotasActivity.class)));

        // TODO: implementar la pantalla
    }
}
