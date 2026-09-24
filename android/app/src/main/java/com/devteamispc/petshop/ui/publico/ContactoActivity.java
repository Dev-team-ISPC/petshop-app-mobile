package com.devteamispc.petshop.ui.publico;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityContactoBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 11 · ContactoActivity
 *
 * Endpoints que consume: POST /contacto/
 *
 * El envío es PÚBLICO: no necesita token.
 * Van los TRES campos: nombre, email y mensaje. Sin ellos la API devuelve 400.
 * Si hay sesión, precargar nombre y email desde sesion.getUsuario().
 * El mensaje tiene mínimo 10 y máximo 500 caracteres: mostrar el contador.
 * El mapa conviene dejarlo como imagen estática: Google Maps pide API key.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_contacto.xml.
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
public class ContactoActivity extends BaseActivity {

    private ActivityContactoBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pantalla pública: no exige sesión.
        vista = ActivityContactoBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
