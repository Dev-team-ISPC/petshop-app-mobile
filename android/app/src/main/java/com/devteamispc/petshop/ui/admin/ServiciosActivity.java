package com.devteamispc.petshop.ui.admin;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityServiciosBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 14 · ServiciosActivity
 *
 * Endpoints que consume: GET · POST · PATCH · DELETE /servicios/
 *
 * SÓLO ADMIN: es el otro CRUD exigido, junto con el de turnos.
 * Un servicio inactivo deja de ofrecerse al pedir turno, pero los turnos
 * ya registrados lo conservan: por eso se desactiva, no se borra.
 * Filtro: ?activo=true
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_servicios.xml.
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
public class ServiciosActivity extends BaseActivity {

    private ActivityServiciosBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityServiciosBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
