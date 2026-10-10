package com.devteamispc.petshop.ui.vacunacion;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityVacunaFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 19 · VacunaFormActivity
 *
 * Endpoints que consume: POST /vacunas/
 *
 * Frecuencia en meses. Sólo veterinario.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_vacuna_form.xml.
 * El manifest, los colores, los estilos y la capa de red ya están hechos:
 * si los modificás, chocás con el resto del equipo en el merge.
 */
public class VacunaFormActivity extends BaseActivity {

    private ActivityVacunaFormBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityVacunaFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
