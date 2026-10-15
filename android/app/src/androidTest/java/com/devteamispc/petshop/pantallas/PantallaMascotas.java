package com.devteamispc.petshop.pantallas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.soporte.Esperar;

/** Screen Object de Mis mascotas. */
public class PantallaMascotas {

    public PantallaMascotas verMascota(String nombre) {
        Esperar.aQueSeVea(allOf(withId(R.id.nombre), withText(nombre)));
        return this;
    }

    public PantallaCarnet abrir(String nombre) {
        onView(allOf(withId(R.id.nombre), withText(nombre))).perform(click());
        return new PantallaCarnet();
    }
}
