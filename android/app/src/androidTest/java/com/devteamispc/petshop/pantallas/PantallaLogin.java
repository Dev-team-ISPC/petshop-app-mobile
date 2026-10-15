package com.devteamispc.petshop.pantallas;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.soporte.Esperar;

/**
 * Screen Object del Login: los tests dicen QUÉ hace el usuario ("ingresar con
 * estos datos"), y esta clase sabe CÓMO se hace en la pantalla (qué campos y
 * botones). Si mañana cambia un id, se corrige acá y no en cada test.
 */
public class PantallaLogin {

    public PantallaLogin ingresar(String email, String password) {
        onView(withId(R.id.campoEmail)).perform(replaceText(email));
        onView(withId(R.id.campoPassword)).perform(replaceText(password));
        closeSoftKeyboard();
        onView(withId(R.id.botonIngresar)).perform(click());
        return this;
    }

    public PantallaLogin verError(String mensaje) {
        Esperar.aQueSeVea(allOf(withId(R.id.textoError), withText(mensaje)));
        return this;
    }

    public PantallaRegistro irARegistro() {
        onView(withId(R.id.linkRegistro)).perform(click());
        return new PantallaRegistro();
    }
}
