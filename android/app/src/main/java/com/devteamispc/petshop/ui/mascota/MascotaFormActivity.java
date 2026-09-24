package com.devteamispc.petshop.ui.mascota;

import android.os.Bundle;

import com.devteamispc.petshop.databinding.ActivityMascotaFormBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 5 · MascotaFormActivity
 *
 * Endpoints que consume: POST /mascotas/  ·  PATCH /mascotas/{id}/
 *
 * Sin campo de dueño: lo asigna el servidor si quien crea es cliente.
 * El peso admite decimales (inputType numberDecimal) y viaja como String.
 * La especie es un catálogo cerrado: perro, gato, ave, conejo, reptil, otro.
 * OJO con el Spinner: se muestra 'Perro' pero se envía 'perro'.
 * La fecha se elige con DatePickerDialog y se convierte con Fechas.aApi().
 *
 * CÓMO TRABAJAR ACÁ
 * Tocá sólo este archivo y su layout res/layout/activity_mascota_form.xml.
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
public class MascotaFormActivity extends BaseActivity {

    private ActivityMascotaFormBinding vista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!exigirSesion()) return;

        vista = ActivityMascotaFormBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        // TODO: implementar la pantalla
    }
}
