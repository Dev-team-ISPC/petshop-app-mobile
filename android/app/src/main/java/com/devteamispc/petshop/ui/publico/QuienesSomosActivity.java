package com.devteamispc.petshop.ui.publico;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityQuienesSomosBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 12 · QuienesSomosActivity
 *
 * Endpoints que consume: (sin API)
 *
 * Acá va el RECURSO MULTIMEDIA que pide el enunciado: un VideoView con un
 * video institucional en res/raw, o un carrusel de imágenes.
 * Es la pantalla que cubre ese requisito, así que no puede quedar sin el medio.
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_quienes_somos.xml.
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
public class QuienesSomosActivity extends BaseActivity {

    private ActivityQuienesSomosBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pantalla pública: no exige sesión.
        vista = ActivityQuienesSomosBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
