package com.devteamispc.petshop.pantallas;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.platform.app.InstrumentationRegistry;

import com.devteamispc.petshop.R;
import com.devteamispc.petshop.soporte.Esperar;

/**
 * Screen Object del Registro.
 *
 * Los ids de la pantalla y los textos de las reglas viven sólo acá: los tests
 * dicen "la regla del largo se cumple", no qué vista ni qué texto la muestran.
 */
public class PantallaRegistro {

    public PantallaRegistro verPantalla() {
        Esperar.aQueSeVea(withId(R.id.campoNombre));
        return this;
    }

    public PantallaRegistro completar(String nombre, String email, String password) {
        onView(withId(R.id.campoNombre)).perform(replaceText(nombre));
        onView(withId(R.id.campoEmail)).perform(replaceText(email));
        onView(withId(R.id.campoPassword)).perform(replaceText(password));
        closeSoftKeyboard();
        return this;
    }

    // ------------------------------------------------ reglas de la contraseña

    public PantallaRegistro verReglaLargo(boolean cumple) {
        return verRegla(R.id.reglaLargo, R.string.regla_largo, cumple);
    }

    public PantallaRegistro verReglaMayusculas(boolean cumple) {
        return verRegla(R.id.reglaMayusculas, R.string.regla_mayusculas, cumple);
    }

    public PantallaRegistro verReglaNumero(boolean cumple) {
        return verRegla(R.id.reglaNumero, R.string.regla_numero, cumple);
    }

    public PantallaRegistro verReglaEspecial(boolean cumple) {
        return verRegla(R.id.reglaEspecial, R.string.regla_especial, cumple);
    }

    /** La regla se muestra con símbolo y texto, por ejemplo "✓  Más de 8 caracteres". */
    private PantallaRegistro verRegla(int idVista, int idTexto, boolean cumple) {
        String texto = InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getString(idTexto);
        onView(withId(idVista)).check(matches(withText((cumple ? "✓  " : "✗  ") + texto)));
        return this;
    }

    // ---------------------------------------------------------------- acciones

    public PantallaRegistro registrarme() {
        onView(withId(R.id.botonRegistrar)).perform(scrollTo(), click());
        return this;
    }

    public PantallaRegistro verError(String mensaje) {
        Esperar.aQueSeVea(allOf(withId(R.id.textoError), withText(mensaje)));
        return this;
    }
}
