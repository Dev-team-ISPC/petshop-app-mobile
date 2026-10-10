package com.devteamispc.petshop.ui.vacunacion;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityVacunasBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 18 · VacunasActivity
 *
 * Endpoints que consume: GET /vacunas/ · DELETE /vacunas/{id}/
 *
 * Catálogo con búsqueda. Sólo veterinario.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_vacunas.xml.
 * El manifest, los colores, los estilos y la capa de red ya están hechos:
 * si los modificás, chocás con el resto del equipo en el merge.
 */
public class VacunasActivity extends BaseActivity {

    private ActivityVacunasBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityVacunasBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
