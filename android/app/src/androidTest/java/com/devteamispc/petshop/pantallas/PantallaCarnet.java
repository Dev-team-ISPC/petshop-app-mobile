package com.devteamispc.petshop.pantallas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.soporte.Esperar;

/** Screen Object del Carnet. */
public class PantallaCarnet {

    public PantallaCarnet verVacuna(String vacuna) {
        Esperar.aQueSeVea(withText(vacuna));
        return this;
    }

    public PantallaCarnet irATurnos() {
        onView(withText(R.string.pestana_turnos)).perform(click());
        return this;
    }

    public PantallaCarnet verTurno(String servicio) {
        Esperar.aQueSeVea(withText(servicio));
        return this;
    }

    /** El cliente no registra vacunas: en la pestaña Vacunas no ve el botón +. */
    public PantallaCarnet verSinBotonAgregar() {
        onView(withId(R.id.botonAgregar)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        return this;
    }

    /** En la pestaña Turnos, el cliente sí pide turnos. */
    public PantallaCarnet verBotonAgregar() {
        onView(withId(R.id.botonAgregar)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        return this;
    }
}
