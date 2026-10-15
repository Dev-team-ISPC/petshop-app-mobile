package com.devteamispc.petshop.pantallas;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.soporte.Esperar;

/** Screen Object del inicio. */
public class PantallaHome {

    public PantallaHome verSaludo(String nombre) {
        Esperar.aQueSeVea(allOf(withId(R.id.textoNombre), withText(nombre)));
        return this;
    }

    public PantallaHome verAcceso(String titulo) {
        Esperar.aQueSeVea(withText(titulo));
        return this;
    }
}
