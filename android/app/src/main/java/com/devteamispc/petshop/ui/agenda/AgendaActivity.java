package com.devteamispc.petshop.ui.agenda;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityAgendaBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 9 · AgendaActivity
 *
 * Endpoints que consume: GET /agenda/  ·  PATCH /turnos/{id}/
 *
 * ATENCIÓN: /agenda/ NO viene paginado. Devuelve {proximas_dosis, turnos}.
 * Dos secciones, ambas ya ordenadas por fecha desde el servidor.
 * Para el veterinario, cada turno lleva los botones Confirmar y Cancelar
 * (PATCH con estado). El cliente sólo ve el estado, y puede cancelar el suyo.
 * Los días restantes vienen calculados: usar Fechas.textoDias().
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_agenda.xml.
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
public class AgendaActivity extends BaseActivity {

    private ActivityAgendaBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityAgendaBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
