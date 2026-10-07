package com.devteamispc.petshop.ui.splash;

import static org.junit.Assert.assertEquals;

import com.devteamispc.petshop.ui.auth.LoginActivity;
import com.devteamispc.petshop.ui.home.HomeActivity;

import org.junit.Test;

/**
 * Tests unitarios de las reglas del splash (patrón AAA: Arrange, Act, Assert).
 * Corren en la JVM, sin emulador: ./gradlew test
 */
public class ReglasSplashTest {

    @Test
    public void esperaRestante_alArrancar_esperaElMinimoCompleto() {
        long espera = ReglasSplash.esperaRestante(0);
        assertEquals(ReglasSplash.TIEMPO_MINIMO_MS, espera);
    }

    @Test
    public void esperaRestante_aMitadDeCamino_esperaLaDiferencia() {
        long espera = ReglasSplash.esperaRestante(1000);
        assertEquals(ReglasSplash.TIEMPO_MINIMO_MS - 1000, espera);
    }

    @Test
    public void esperaRestante_justoEnElLimite_noEspera() {
        long espera = ReglasSplash.esperaRestante(ReglasSplash.TIEMPO_MINIMO_MS);
        assertEquals(0, espera);
    }

    @Test
    public void esperaRestante_servidorLento_nuncaDevuelveNegativo() {
        long espera = ReglasSplash.esperaRestante(40000);
        assertEquals(0, espera);
    }

    @Test
    public void esperaRestante_tiempoInvalido_esperaElMinimo() {
        long espera = ReglasSplash.esperaRestante(-5);
        assertEquals(ReglasSplash.TIEMPO_MINIMO_MS, espera);
    }

    @Test
    public void destino_conSesion_vaAlHome() {
        assertEquals(HomeActivity.class, ReglasSplash.destino(true));
    }

    @Test
    public void destino_sinSesion_vaAlLogin() {
        assertEquals(LoginActivity.class, ReglasSplash.destino(false));
    }
}
