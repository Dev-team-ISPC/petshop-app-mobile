package com.devteamispc.petshop.ui.admin;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityUsuariosBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 13 · UsuariosActivity
 *
 * Endpoints que consume: GET · POST · PATCH · DELETE /usuarios/
 *
 * SÓLO ADMIN: es uno de los dos CRUD que exige la consigna.
 * Filtros disponibles: ?search= por nombre y email, ?rol= y ?activo=.
 * Al crear un usuario, la contraseña pasa por la misma política que el registro.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_usuarios.xml.
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
public class UsuariosActivity extends BaseActivity {

    private ActivityUsuariosBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityUsuariosBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
