package com.devteamispc.petshop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * AUT-UNIT-02 · Validadores del formulario de mascota (US04, RF04).
 *
 * El peso se escribe con coma y viaja a la API con punto y dos decimales;
 * rango válido: mayor a 0 y hasta 999,99 kg. Valores límite y patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.util.ValidacionesMascotaTest"
 */
public class ValidacionesMascotaTest {

    @Test
    public void peso_conComa_seNormalizaParaLaApi() {
        // Arrange
        String escrito = "28,5";

        // Act
        String paraApi = Validaciones.pesoParaApi(escrito);

        // Assert
        assertEquals("28.50", paraApi);
    }

    @Test
    public void peso_cero_esInvalido() {
        // Arrange: límite inferior, excluido
        String escrito = "0";

        // Act
        String paraApi = Validaciones.pesoParaApi(escrito);

        // Assert
        assertNull(paraApi);
    }

    @Test
    public void peso_negativo_esInvalido() {
        // Arrange
        String escrito = "-3";

        // Act
        String paraApi = Validaciones.pesoParaApi(escrito);

        // Assert
        assertNull(paraApi);
    }

    @Test
    public void peso_enElMaximo_esValido() {
        // Arrange: límite superior, incluido
        String escrito = "999,99";

        // Act
        String paraApi = Validaciones.pesoParaApi(escrito);

        // Assert
        assertEquals("999.99", paraApi);
    }

    @Test
    public void peso_porEncimaDelMaximo_esInvalido() {
        // Arrange
        String escrito = "1000";

        // Act
        String paraApi = Validaciones.pesoParaApi(escrito);

        // Assert
        assertNull(paraApi);
    }

    @Test
    public void peso_conTresDecimales_esInvalido() {
        // Arrange: fuera de formato
        String escrito = "4,205";

        // Act
        String paraApi = Validaciones.pesoParaApi(escrito);

        // Assert
        assertNull(paraApi);
    }

    @Test
    public void esPosterior_fechaSiguiente_esVerdadero() {
        // Arrange
        String proxima = "2027-01-01";
        String aplicacion = "2026-12-31";

        // Act
        boolean posterior = Validaciones.esPosterior(proxima, aplicacion);

        // Assert
        assertTrue(posterior);
    }

    @Test
    public void esPosterior_mismaFecha_esFalso() {
        // Arrange: límite, la misma fecha no cuenta como posterior
        String fecha = "2026-12-31";

        // Act
        boolean posterior = Validaciones.esPosterior(fecha, fecha);

        // Assert
        assertFalse(posterior);
    }
}
