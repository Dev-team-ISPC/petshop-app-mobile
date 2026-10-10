package com.devteamispc.petshop.ui.admin;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityConsultasBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 17 · ConsultasActivity
 *
 * Endpoints que consume: GET /contacto/ · PATCH /contacto/{id}/
 *
 * Mensajes del formulario de Contacto, con filtro de no leídas. Sólo administrador.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_consultas.xml.
 * El manifest, los colores, los estilos y la capa de red ya están hechos:
 * si los modificás, chocás con el resto del equipo en el merge.
 */
public class ConsultasActivity extends BaseActivity {

    private ActivityConsultasBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityConsultasBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
