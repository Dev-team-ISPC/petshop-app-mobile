package com.devteamispc.petshop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * AUT-UNIT-05 · Sugerencia de la próxima dosis al registrar una vacunación (US06, RF07).
 *
 * La frecuencia del catálogo ("anual", "cada 6 meses") se traduce a meses y
 * se suma a la fecha de aplicación. Valores límite (fin de mes, cambio de año) y patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.util.ReglasVacunacionTest"
 */
public class ReglasVacunacionTest {

    // ----------------------------------------------- frecuencia en meses

    @Test
    public void frecuencia_anual_son12Meses() {
        // Arrange
        String frecuencia = "Anual";

        // Act
        Integer meses = ReglasVacunacion.mesesDeFrecuencia(frecuencia);

        // Assert
        assertEquals(Integer.valueOf(12), meses);
    }

    @Test
    public void frecuencia_cada6Meses_son6() {
        // Arrange
        String frecuencia = "cada 6 meses";

        // Act
        Integer meses = ReglasVacunacion.mesesDeFrecuencia(frecuencia);

        // Assert
        assertEquals(Integer.valueOf(6), meses);
    }

    @Test
    public void frecuencia_cada2Anios_son24() {
        // Arrange
        String frecuencia = "cada 2 años";

        // Act
        Integer meses = ReglasVacunacion.mesesDeFrecuencia(frecuencia);

        // Assert
        assertEquals(Integer.valueOf(24), meses);
    }

    @Test
    public void frecuencia_textoLibre_noSugiere() {
        // Arrange
        String frecuencia = "según criterio veterinario";

        // Act
        Integer meses = ReglasVacunacion.mesesDeFrecuencia(frecuencia);

        // Assert
        assertNull(meses);
    }

    @Test
    public void frecuencia_nula_noSugiere() {
        // Arrange
        String frecuencia = null;

        // Act
        Integer meses = ReglasVacunacion.mesesDeFrecuencia(frecuencia);

        // Assert
        assertNull(meses);
    }

    // ----------------------------------------------------- suma de meses

    @Test
    public void sumarMeses_quePasaDeAnio_cruzaElAnio() {
        // Arrange
        String aplicacion = "2026-09-18";

        // Act
        String proxima = ReglasVacunacion.sumarMeses(aplicacion, 6);

        // Assert
        assertEquals("2027-03-18", proxima);
    }

    @Test
    public void sumarMeses_desdeFinDeMes_quedaEnElUltimoDia() {
        // Arrange
        // Límite: enero tiene 31 días y febrero de 2026, 28.
        String aplicacion = "2026-01-31";

        // Act
        String proxima = ReglasVacunacion.sumarMeses(aplicacion, 1);

        // Assert
        assertEquals("2026-02-28", proxima);
    }

    @Test
    public void sugerirProximaDosis_anual_sumaUnAnio() {
        // Arrange
        String aplicacion = "2026-10-05";
        String frecuencia = "anual";

        // Act
        String proxima = ReglasVacunacion.sugerirProximaDosis(aplicacion, frecuencia);

        // Assert
        assertEquals("2027-10-05", proxima);
    }

    // -------------------------------------------- texto de la frecuencia

    @Test
    public void frecuenciaDesdeMeses_doce_esAnual() {
        // Arrange
        int meses = 12;

        // Act
        String texto = ReglasVacunacion.frecuenciaDesdeMeses(meses);

        // Assert
        assertEquals("anual", texto);
    }

    @Test
    public void frecuenciaDesdeMeses_uno_esMensual() {
        // Arrange
        int meses = 1;

        // Act
        String texto = ReglasVacunacion.frecuenciaDesdeMeses(meses);

        // Assert
        assertEquals("mensual", texto);
    }

    @Test
    public void frecuenciaDesdeMeses_seis_esCadaSeisMeses() {
        // Arrange
        int meses = 6;

        // Act
        String texto = ReglasVacunacion.frecuenciaDesdeMeses(meses);

        // Assert
        assertEquals("cada 6 meses", texto);
    }

    @Test
    public void frecuenciaDesdeMeses_idaYVuelta_seVuelveAEntender() {
        // Arrange
        int[] casos = {1, 3, 6, 12, 18, 24};
        Integer[] obtenidos = new Integer[casos.length];

        // Act
        for (int i = 0; i < casos.length; i++) {
            obtenidos[i] = ReglasVacunacion.mesesDeFrecuencia(ReglasVacunacion.frecuenciaDesdeMeses(casos[i]));
        }

        // Assert
        for (int i = 0; i < casos.length; i++) {
            assertEquals(Integer.valueOf(casos[i]), obtenidos[i]);
        }
    }
}
