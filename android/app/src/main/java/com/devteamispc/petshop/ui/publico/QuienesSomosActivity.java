package com.devteamispc.petshop.ui.publico;

import android.os.Bundle;

import androidx.core.view.ViewCompat;

import com.devteamispc.petshop.databinding.ActivityQuienesSomosBinding;
import com.devteamispc.petshop.ui.BaseActivity;

/**
 * 12 · QuienesSomosActivity — recurso multimedia (consigna)
 *
 * Sin API: pantalla institucional con una imagen (res/drawable-nodpi/
 * imagen_institucional.png), la historia de la veterinaria, dirección y horario.
 * La consigna pide "algún tipo de recurso multimedia en una nueva Activity
 * (video y/o imagen)": esta es esa Activity.
 */
public class QuienesSomosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pantalla pública: no exige sesión.

        ActivityQuienesSomosBinding vista = ActivityQuienesSomosBinding.inflate(getLayoutInflater());
        setContentView(vista.getRoot());
        mostrarFlechaAtras();

        ViewCompat.setAccessibilityHeading(vista.textoTitulo, true);
    }
}
