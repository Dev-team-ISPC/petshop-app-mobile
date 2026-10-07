package com.devteamispc.petshop.ui.splash;

import android.app.Activity;

import com.devteamispc.petshop.ui.auth.LoginActivity;
import com.devteamispc.petshop.ui.home.HomeActivity;

/**
 * Las decisiones del splash, separadas de la Activity para poder probarlas
 * con tests unitarios en la JVM, sin emulador (AUT-UNIT).
 */
public final class ReglasSplash {

    /** Tiempo mínimo que se ve el logo, aunque el servidor ya esté despierto. */
    public static final long TIEMPO_MINIMO_MS = 1500;
    /** A partir de cuándo se avisa que el servidor está tardando. */
    public static final long AVISO_LENTO_MS = 3000;
    /** Tope de espera: pasado este tiempo se sigue igual. */
    public static final long TIEMPO_MAXIMO_MS = 65000;

    private ReglasSplash() { }

    /**
     * Cuánto falta esperar para cumplir el tiempo mínimo del logo.
     * Nunca devuelve un valor negativo.
     */
    public static long esperaRestante(long transcurridoMs) {
        if (transcurridoMs < 0) return TIEMPO_MINIMO_MS;
        return Math.max(0, TIEMPO_MINIMO_MS - transcurridoMs);
    }

    /** A qué pantalla ir después del splash. */
    public static Class<? extends Activity> destino(boolean haySesion) {
        return haySesion ? HomeActivity.class : LoginActivity.class;
    }
}
