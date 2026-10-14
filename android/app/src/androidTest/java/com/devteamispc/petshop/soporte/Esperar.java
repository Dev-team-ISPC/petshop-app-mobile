package com.devteamispc.petshop.soporte;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.os.SystemClock;
import android.view.View;

import org.hamcrest.Matcher;

/**
 * Espera a que aparezca una vista.
 *
 * Espresso espera solo a la interfaz, pero no a los pedidos de red que hace
 * OkHttp en segundo plano. Como el servidor simulado responde en milisegundos,
 * alcanza con reintentar unos segundos antes de dar el paso por fallido.
 */
public final class Esperar {

    private static final long TOPE_MS = 5000;
    private static final long PAUSA_MS = 100;

    private Esperar() { }

    public static void aQueSeVea(Matcher<View> vista) {
        long limite = SystemClock.uptimeMillis() + TOPE_MS;
        while (true) {
            try {
                onView(vista).check(matches(isDisplayed()));
                return;
            } catch (Throwable error) {
                if (SystemClock.uptimeMillis() > limite) throw error;
                SystemClock.sleep(PAUSA_MS);
            }
        }
    }
}
