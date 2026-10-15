package com.devteamispc.petshop.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * AUT-UNIT-04 · Duración de un servicio (CRUD de servicios).
 * Rango válido: de DURACION_MINIMA a DURACION_MAXIMA minutos. Valores límite y patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.util.ValidacionesServicioTest"
 */
public class ValidacionesServicioTest {

    @Test
    public void duracion_textoNoNumerico_esNula() {
        // Arrange
        String escrito = "media hora";

        // Act
        Integer minutos = Validaciones.duracion(escrito);

        // Assert
        assertNull(minutos);
    }

    @Test
    public void duracion_enElMinimo_esValida() {
        // Arrange
        Integer minutos = Validaciones.DURACION_MINIMA;

        // Act
        boolean valida = Validaciones.duracionValida(minutos);

        // Assert
        assertTrue(valida);
    }

    @Test
    public void duracion_enElMaximo_esValida() {
        // Arrange
        Integer minutos = Validaciones.DURACION_MAXIMA;

        // Act
        boolean valida = Validaciones.duracionValida(minutos);

        // Assert
        assertTrue(valida);
    }

    @Test
    public void duracion_unoMenosQueElMinimo_esInvalida() {
        // Arrange
        Integer minutos = Validaciones.DURACION_MINIMA - 1;

        // Act
        boolean valida = Validaciones.duracionValida(minutos);

        // Assert
        assertFalse(valida);
    }

    @Test
    public void duracion_unoMasQueElMaximo_esInvalida() {
        // Arrange
        Integer minutos = Validaciones.DURACION_MAXIMA + 1;

        // Act
        boolean valida = Validaciones.duracionValida(minutos);

        // Assert
        assertFalse(valida);
    }

    @Test
    public void duracion_vacia_esInvalida() {
        // Arrange
        Integer minutos = null;

        // Act
        boolean valida = Validaciones.duracionValida(minutos);

        // Assert
        assertFalse(valida);
    }
}
