package com.devteamispc.petshop.ui.perfil;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityPerfilBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 10 · PerfilActivity
 *
 * Endpoints que consume: GET /auth/me/  ·  PATCH /auth/me/  ·  DELETE /auth/me/
 *
 * El email no se puede editar: mostrarlo deshabilitado.
 * El DELETE es el botón de arrepentimiento que exige ciberseguridad:
 * va en un bloque aparte, en rojo, y con confirmación antes de ejecutarlo.
 * Después de eliminar o cerrar sesión: sesion.cerrarSesion() y volver al login.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_perfil.xml.
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
public class PerfilActivity extends BaseActivity {

    private ActivityPerfilBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
