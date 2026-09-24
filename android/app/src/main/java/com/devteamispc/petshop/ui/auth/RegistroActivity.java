package com.devteamispc.petshop.ui.auth;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityRegistroBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 2 · RegistroActivity
 *
 * Endpoints que consume: POST /auth/registro/
 *
 * Mostrar las cuatro reglas de la contraseña: más de 8 caracteres, mayúscula,
 * minúscula, número y carácter especial. Si el backend devuelve 400, el detalle
 * de qué regla falló viene en el campo password: usar ApiError.mensajeDeCampo().
 * El checkbox de términos es obligatorio: sin él la API rechaza el alta.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_registro.xml.
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
public class RegistroActivity extends BaseActivity {

    private ActivityRegistroBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
