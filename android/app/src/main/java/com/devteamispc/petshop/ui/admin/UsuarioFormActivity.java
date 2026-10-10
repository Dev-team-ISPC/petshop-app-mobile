package com.devteamispc.petshop.ui.admin;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityUsuarioFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 15 · UsuarioFormActivity
 *
 * Endpoints que consume: POST /usuarios/ · PATCH /usuarios/{id}/ · DELETE /usuarios/{id}/
 *
 * Sin EXTRA_USUARIO_ID es un alta; con él, una edición. Sólo administrador.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_usuario_form.xml.
 * El manifest, los colores, los estilos y la capa de red ya están hechos:
 * si los modificás, chocás con el resto del equipo en el merge.
 */
public class UsuarioFormActivity extends BaseActivity {

    public static final String EXTRA_USUARIO_ID = "usuario_id";

    private ActivityUsuarioFormBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityUsuarioFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
