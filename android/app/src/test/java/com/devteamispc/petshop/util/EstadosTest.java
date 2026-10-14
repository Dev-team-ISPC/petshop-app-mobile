package com.devteamispc.petshop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.devteamispc.petshop.data.model.Turno;

import org.junit.Test;

/**
 * AUT-UNIT-04 · Reglas de turnos y agenda (US13, US14, RF13, RF17).
 *
 * Qué color lleva cada dosis y cada turno, y quién puede confirmar, completar
 * o cancelar según su rol y el estado del turno. Valores límite y patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.util.EstadosTest"
 */
public class EstadosTest {

    // ----------------------------------------- nivel de la próxima dosis

    @Test
    public void nivelDosis_sinFecha_esNeutro() {
        // Arrange
        Integer dias = null;

        // Act
        Estados.Nivel nivel = Estados.nivelDosis(dias);

        // Assert
        assertEquals(Estados.Nivel.NEUTRO, nivel);
    }

    @Test
    public void nivelDosis_vencida_esPeligro() {
        // Arrange
        Integer dias = -1;

        // Act
        Estados.Nivel nivel = Estados.nivelDosis(dias);

        // Assert
        assertEquals(Estados.Nivel.PELIGRO, nivel);
    }

    @Test
    public void nivelDosis_hoy_esAtencion() {
        // Arrange
        Integer dias = 0;

        // Act
        Estados.Nivel nivel = Estados.nivelDosis(dias);

        // Assert
        assertEquals(Estados.Nivel.ATENCION, nivel);
    }

    @Test
    public void nivelDosis_justoEnElLimiteDeAviso_esAtencion() {
        // Arrange
        Integer dias = Estados.DIAS_AVISO_DOSIS;

        // Act
        Estados.Nivel nivel = Estados.nivelDosis(dias);

        // Assert
        assertEquals(Estados.Nivel.ATENCION, nivel);
    }

    @Test
    public void nivelDosis_unDiaDespuesDelLimite_esNeutro() {
        // Arrange
        Integer dias = Estados.DIAS_AVISO_DOSIS + 1;

        // Act
        Estados.Nivel nivel = Estados.nivelDosis(dias);

        // Assert
        assertEquals(Estados.Nivel.NEUTRO, nivel);
    }

    // --------------------------------------------------- nivel del turno

    @Test
    public void nivelTurno_pendiente_esAtencion() {
        // Arrange
        String estado = Turno.PENDIENTE;

        // Act
        Estados.Nivel nivel = Estados.nivelTurno(estado);

        // Assert
        assertEquals(Estados.Nivel.ATENCION, nivel);
    }

    @Test
    public void nivelTurno_confirmado_esOk() {
        // Arrange
        String estado = Turno.CONFIRMADO;

        // Act
        Estados.Nivel nivel = Estados.nivelTurno(estado);

        // Assert
        assertEquals(Estados.Nivel.OK, nivel);
    }

    @Test
    public void nivelTurno_cancelado_esPeligro() {
        // Arrange
        String estado = Turno.CANCELADO;

        // Act
        Estados.Nivel nivel = Estados.nivelTurno(estado);

        // Assert
        assertEquals(Estados.Nivel.PELIGRO, nivel);
    }

    @Test
    public void nivelTurno_completado_esNeutro() {
        // Arrange
        String estado = Turno.COMPLETADO;

        // Act
        Estados.Nivel nivel = Estados.nivelTurno(estado);

        // Assert
        assertEquals(Estados.Nivel.NEUTRO, nivel);
    }

    // --------------------------------------------------------- confirmar

    @Test
    public void puedeConfirmar_veterinarioConTurnoPendiente_si() {
        // Arrange
        boolean esVeterinario = true;
        String estado = Turno.PENDIENTE;

        // Act
        boolean puede = Estados.puedeConfirmar(esVeterinario, estado);

        // Assert
        assertTrue(puede);
    }

    @Test
    public void puedeConfirmar_veterinarioConTurnoYaConfirmado_no() {
        // Arrange
        boolean esVeterinario = true;
        String estado = Turno.CONFIRMADO;

        // Act
        boolean puede = Estados.puedeConfirmar(esVeterinario, estado);

        // Assert
        assertFalse(puede);
    }

    @Test
    public void puedeConfirmar_cliente_nunca() {
        // Arrange
        boolean esVeterinario = false;
        String estado = Turno.PENDIENTE;

        // Act
        boolean puede = Estados.puedeConfirmar(esVeterinario, estado);

        // Assert
        assertFalse(puede);
    }

    // --------------------------------------------------------- completar

    @Test
    public void puedeCompletar_veterinarioConTurnoConfirmado_si() {
        // Arrange
        boolean esVeterinario = true;
        String estado = Turno.CONFIRMADO;

        // Act
        boolean puede = Estados.puedeCompletar(esVeterinario, estado);

        // Assert
        assertTrue(puede);
    }

    @Test
    public void puedeCompletar_turnoPendiente_no() {
        // Arrange
        boolean esVeterinario = true;
        String estado = Turno.PENDIENTE;

        // Act
        boolean puede = Estados.puedeCompletar(esVeterinario, estado);

        // Assert
        assertFalse(puede);
    }

    @Test
    public void puedeCompletar_cliente_nunca() {
        // Arrange
        boolean esVeterinario = false;
        String estado = Turno.CONFIRMADO;

        // Act
        boolean puede = Estados.puedeCompletar(esVeterinario, estado);

        // Assert
        assertFalse(puede);
    }

    // ---------------------------------------------------------- cancelar

    @Test
    public void puedeCancelar_clienteConTurnoPendiente_si() {
        // Arrange
        boolean esVeterinario = false;
        boolean esCliente = true;
        String estado = Turno.PENDIENTE;

        // Act
        boolean puede = Estados.puedeCancelar(esVeterinario, esCliente, estado);

        // Assert
        assertTrue(puede);
    }

    @Test
    public void puedeCancelar_veterinarioConTurnoConfirmado_si() {
        // Arrange
        boolean esVeterinario = true;
        boolean esCliente = false;
        String estado = Turno.CONFIRMADO;

        // Act
        boolean puede = Estados.puedeCancelar(esVeterinario, esCliente, estado);

        // Assert
        assertTrue(puede);
    }

    @Test
    public void puedeCancelar_clienteConTurnoCancelado_no() {
        // Arrange
        boolean esVeterinario = false;
        boolean esCliente = true;
        String estado = Turno.CANCELADO;

        // Act
        boolean puede = Estados.puedeCancelar(esVeterinario, esCliente, estado);

        // Assert
        assertFalse(puede);
    }

    @Test
    public void puedeCancelar_veterinarioConTurnoCompletado_no() {
        // Arrange
        boolean esVeterinario = true;
        boolean esCliente = false;
        String estado = Turno.COMPLETADO;

        // Act
        boolean puede = Estados.puedeCancelar(esVeterinario, esCliente, estado);

        // Assert
        assertFalse(puede);
    }

    @Test
    public void puedeCancelar_administrador_soloConsulta() {
        // Arrange
        boolean esVeterinario = false;
        boolean esCliente = false;
        String estado = Turno.PENDIENTE;

        // Act
        boolean puede = Estados.puedeCancelar(esVeterinario, esCliente, estado);

        // Assert
        assertFalse(puede);
    }

    // ---------------------------------------------------------- etiqueta

    @Test
    public void etiquetaEstado_pendiente_seCapitaliza() {
        // Arrange
        String estado = Turno.PENDIENTE;

        // Act
        String etiqueta = Estados.etiquetaEstado(estado);

        // Assert
        assertEquals("Pendiente", etiqueta);
    }

    @Test
    public void etiquetaEstado_nulo_devuelveVacio() {
        // Arrange
        String estado = null;

        // Act
        String etiqueta = Estados.etiquetaEstado(estado);

        // Assert
        assertEquals("", etiqueta);
    }

    @Test
    public void etiquetaEstado_vacio_devuelveVacio() {
        // Arrange
        String estado = "";

        // Act
        String etiqueta = Estados.etiquetaEstado(estado);

        // Assert
        assertEquals("", etiqueta);
    }
}
