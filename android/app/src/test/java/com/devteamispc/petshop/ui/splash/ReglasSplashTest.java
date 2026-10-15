package com.devteamispc.petshop.ui.splash;

import static org.junit.Assert.assertEquals;

import com.devteamispc.petshop.ui.auth.LoginActivity;
import com.devteamispc.petshop.ui.home.HomeActivity;

import org.junit.Test;

/**
 * AUT-UNIT-06 · Reglas del splash.
 *
 * El splash se muestra un tiempo mínimo (mientras despierta el servidor de
 * Render) y después deriva al inicio o al login según haya sesión guardada.
 * Valores límite y patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.ui.splash.ReglasSplashTest"
 */
public class ReglasSplashTest {

    @Test
    public void esperaRestante_alArrancar_esperaElMinimoCompleto() {
        // Arrange
        long transcurrido = 0;

        // Act
        long espera = ReglasSplash.esperaRestante(transcurrido);

        // Assert
        assertEquals(ReglasSplash.TIEMPO_MINIMO_MS, espera);
    }

    @Test
    public void esperaRestante_aMitadDeCamino_esperaLaDiferencia() {
        // Arrange
        long transcurrido = 1000;

        // Act
        long espera = ReglasSplash.esperaRestante(transcurrido);

        // Assert
        assertEquals(ReglasSplash.TIEMPO_MINIMO_MS - 1000, espera);
    }

    @Test
    public void esperaRestante_justoEnElLimite_noEspera() {
        // Arrange
        long transcurrido = ReglasSplash.TIEMPO_MINIMO_MS;

        // Act
        long espera = ReglasSplash.esperaRestante(transcurrido);

        // Assert
        assertEquals(0, espera);
    }

    @Test
    public void esperaRestante_servidorLento_nuncaDevuelveNegativo() {
        // Arrange
        long transcurrido = 40000;

        // Act
        long espera = ReglasSplash.esperaRestante(transcurrido);

        // Assert
        assertEquals(0, espera);
    }

    @Test
    public void esperaRestante_tiempoNegativo_esperaElMinimo() {
        // Arrange
        long transcurrido = -5;

        // Act
        long espera = ReglasSplash.esperaRestante(transcurrido);

        // Assert
        assertEquals(ReglasSplash.TIEMPO_MINIMO_MS, espera);
    }

    @Test
    public void destino_conSesion_vaAlInicio() {
        // Arrange
        boolean haySesion = true;

        // Act
        Class<?> destino = ReglasSplash.destino(haySesion);

        // Assert
        assertEquals(HomeActivity.class, destino);
    }

    @Test
    public void destino_sinSesion_vaAlLogin() {
        // Arrange
        boolean haySesion = false;

        // Act
        Class<?> destino = ReglasSplash.destino(haySesion);

        // Assert
        assertEquals(LoginActivity.class, destino);
    }
}
