package com.devteamispc.petshop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.TimeZone;

/**
 * AUT-UNIT-03 · Conversión de las fechas de la API (RF13, agenda y carnet).
 *
 * Django manda fecha y hora en ISO 8601 (con zona o en UTC) y fechas como
 * yyyy-MM-dd; la app las muestra en hora argentina. La zona va explícita para
 * que el resultado no dependa de la máquina que corre el test. Patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.util.FechasTest"
 */
public class FechasTest {

    private static final TimeZone ARGENTINA = TimeZone.getTimeZone("America/Argentina/Buenos_Aires");

    // -------------------------------------------- fecha y hora de la API

    @Test
    public void fechaHora_conZonaDeDjango_seMuestraEnHoraLocal() {
        // Arrange
        String api = "2026-09-23T15:30:00-03:00";

        // Act
        String visible = Fechas.fechaHoraAVisible(api, ARGENTINA);

        // Assert
        assertEquals("23/09/2026 · 15:30", visible);
    }

    @Test
    public void fechaHora_enUtc_seConvierteAHoraArgentina() {
        // Arrange
        String api = "2026-09-23T18:30:00Z";

        // Act
        String visible = Fechas.fechaHoraAVisible(api, ARGENTINA);

        // Assert
        assertEquals("23/09/2026 · 15:30", visible);
    }

    @Test
    public void fechaHora_conMicrosegundos_seEntiende() {
        // Arrange
        String api = "2026-10-05T10:12:34.123456-03:00";

        // Act
        String visible = Fechas.fechaHoraAVisible(api, ARGENTINA);

        // Assert
        assertEquals("05/10/2026 · 10:12", visible);
    }

    @Test
    public void fechaHora_textoInvalido_devuelveElTextoOriginal() {
        // Arrange
        String api = "mañana a la tarde";

        // Act
        String visible = Fechas.fechaHoraAVisible(api, ARGENTINA);

        // Assert
        assertEquals("mañana a la tarde", visible);
    }

    @Test
    public void parsearIso_fechaSinHora_esNulo() {
        // Arrange
        String api = "2026-09-23";

        // Act
        Long instante = Fechas.parsearIso(api);

        // Assert
        assertNull(instante);
    }

    @Test
    public void parsearIso_mismoInstanteEnUtcYEnArgentina_esIgual() {
        // Arrange
        // Lo que arma el formulario (UTC con Z) tiene que leerse como el mismo instante.
        String enviado = "2026-10-13T13:00:00Z";
        String recibido = "2026-10-13T10:00:00-03:00";

        // Act
        Long instanteEnviado = Fechas.parsearIso(enviado);
        Long instanteRecibido = Fechas.parsearIso(recibido);

        // Assert
        assertEquals(instanteEnviado, instanteRecibido);
    }

    // ---------------------------------------------------- fecha relativa

    @Test
    public void relativa_mismoDia_diceHoy() {
        // Arrange
        long ahora = Fechas.parsearIso("2026-10-05T12:00:00-03:00");
        String api = "2026-10-05T10:12:00-03:00";

        // Act
        String texto = Fechas.fechaHoraRelativa(api, ahora, ARGENTINA);

        // Assert
        assertEquals("Hoy 10:12", texto);
    }

    @Test
    public void relativa_diaAnterior_diceAyer() {
        // Arrange
        long ahora = Fechas.parsearIso("2026-10-05T12:00:00-03:00");
        String api = "2026-10-04T17:40:00-03:00";

        // Act
        String texto = Fechas.fechaHoraRelativa(api, ahora, ARGENTINA);

        // Assert
        assertEquals("Ayer 17:40", texto);
    }

    @Test
    public void relativa_otroDia_muestraDiaYMes() {
        // Arrange
        long ahora = Fechas.parsearIso("2026-10-05T12:00:00-03:00");
        String api = "2026-09-15T09:05:00-03:00";

        // Act
        String texto = Fechas.fechaHoraRelativa(api, ahora, ARGENTINA);

        // Assert
        assertEquals("15/09 09:05", texto);
    }

    // -------------------------------------------- días restantes (badge)

    @Test
    public void textoDias_cero_diceHoy() {
        // Arrange
        Integer dias = 0;

        // Act
        String texto = Fechas.textoDias(dias);

        // Assert
        assertEquals("Hoy", texto);
    }

    @Test
    public void textoDias_uno_diceManiana() {
        // Arrange
        Integer dias = 1;

        // Act
        String texto = Fechas.textoDias(dias);

        // Assert
        assertEquals("Mañana", texto);
    }

    @Test
    public void textoDias_varios_diceEnNDias() {
        // Arrange
        Integer dias = 25;

        // Act
        String texto = Fechas.textoDias(dias);

        // Assert
        assertEquals("En 25 días", texto);
    }

    @Test
    public void textoDias_negativo_diceVencida() {
        // Arrange
        Integer dias = -3;

        // Act
        String texto = Fechas.textoDias(dias);

        // Assert
        assertEquals("Vencida hace 3 días", texto);
    }

    // ---------------------------------------------------- fecha sin hora

    @Test
    public void fecha_deLaApi_seMuestraComoDiaMesAnio() {
        // Arrange
        String api = "2026-10-13";

        // Act
        String visible = Fechas.aVisible(api);

        // Assert
        assertEquals("13/10/2026", visible);
    }

    @Test
    public void fecha_visible_seMandaALaApiComoIso() {
        // Arrange
        String visible = "13/10/2026";

        // Act
        String api = Fechas.aApi(visible);

        // Assert
        assertEquals("2026-10-13", api);
    }
}
