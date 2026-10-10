package com.devteamispc.petshop.ui.admin;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityServicioFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 16 · ServicioFormActivity
 *
 * Endpoints que consume: POST /servicios/ · PATCH /servicios/{id}/ · DELETE /servicios/{id}/
 *
 * El listado le pasa los datos del servicio por extras, para no pedirlo de nuevo.
 * Sólo administrador.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_servicio_form.xml.
 * El manifest, los colores, los estilos y la capa de red ya están hechos:
 * si los modificás, chocás con el resto del equipo en el merge.
 */
public class ServicioFormActivity extends BaseActivity {

    public static final String EXTRA_SERVICIO_ID = "servicio_id";
    public static final String EXTRA_NOMBRE = "servicio_nombre";
    public static final String EXTRA_DESCRIPCION = "servicio_descripcion";
    public static final String EXTRA_DURACION = "servicio_duracion";
    public static final String EXTRA_ACTIVO = "servicio_activo";

    private ActivityServicioFormBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityServicioFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
