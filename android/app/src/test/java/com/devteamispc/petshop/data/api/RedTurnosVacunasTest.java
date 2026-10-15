package com.devteamispc.petshop.data.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.Turno;
import com.devteamispc.petshop.data.model.Vacunacion;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;

/**
 * AUT-NET-03 · Carnet: turnos y vacunaciones de una mascota contra MockWebServer (US05, US06).
 *
 * Cubre el mínimo AUT-NET-02 del Sprint 2: una respuesta 200 que se convierte
 * bien en objetos del modelo, y una respuesta 500 que la app maneja sin romperse.
 * Corren en la JVM, sin emulador ni internet. Patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.data.api.RedTurnosVacunasTest"
 */
public class RedTurnosVacunasTest {

    /** Almacén de tokens en memoria: reemplaza a SessionManager en los tests. */
    private static class TokensEnMemoria implements AlmacenTokens {
        private String access = "access-de-prueba";
        private String refresh = "refresh-de-prueba";

        @Override public String getAccess() { return access; }
        @Override public String getRefresh() { return refresh; }
        @Override public void actualizarTokens(String a, String r) { access = a; refresh = r; }
        @Override public void cerrarSesion() { access = null; refresh = null; }
    }

    private static final String PAGINA_TURNOS = "{\"count\":1,\"next\":null,\"previous\":null,\"results\":["
            + "{\"id\":4,\"mascota\":1,\"mascota_nombre\":\"Rocco\",\"dueno\":3,\"dueno_nombre\":\"Juan Pérez\","
            + "\"servicio\":1,\"servicio_nombre\":\"Consulta general\",\"veterinario\":null,\"veterinario_nombre\":null,"
            + "\"fecha\":\"2026-10-20T15:30:00-03:00\",\"dias_restantes\":6,\"estado\":\"pendiente\","
            + "\"estado_display\":\"Pendiente\",\"observaciones\":\"\"}]}";

    private static final String PAGINA_VACUNACIONES = "{\"count\":1,\"next\":null,\"previous\":null,\"results\":["
            + "{\"id\":7,\"mascota\":1,\"mascota_nombre\":\"Rocco\",\"vacuna\":1,\"vacuna_nombre\":\"Quíntuple\","
            + "\"fecha_aplicacion\":\"2025-10-13\",\"proxima_dosis\":\"2026-10-13\",\"dias_para_proxima_dosis\":0,"
            + "\"veterinario\":2,\"veterinario_nombre\":\"Dra. Gómez\"}]}";

    /** Lo que devuelve Django con DEBUG=False ante un error interno: HTML, no JSON. */
    private static final String ERROR_500_DE_DJANGO = "<!doctype html><html><body><h1>Server Error (500)</h1></body></html>";

    private MockWebServer servidor;
    private PetshopApi api;

    @Before
    public void preparar() throws Exception {
        servidor = new MockWebServer();
        servidor.start();
        api = ApiClient.construirCon(servidor.url("/api/").toString(), new TokensEnMemoria());
    }

    @After
    public void cerrar() throws Exception {
        servidor.shutdown();
    }

    @Test
    public void turnosDeMascota_respuesta200_seConviertenEnObjetos() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setResponseCode(200).setBody(PAGINA_TURNOS));

        // Act
        Response<Pagina<Turno>> r = api.turnosDeMascota(1).execute();

        // Assert
        assertEquals("/api/mascotas/1/turnos/", servidor.takeRequest().getPath());
        Turno turno = r.body().getResults().get(0);
        assertEquals("Consulta general", turno.getServicioNombre());
        assertEquals("2026-10-20T15:30:00-03:00", turno.getFecha());
        assertEquals(Turno.PENDIENTE, turno.getEstado());
        assertNull(turno.getVeterinario());
    }

    @Test
    public void vacunacionesDeMascota_respuesta200_seConviertenEnObjetos() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setResponseCode(200).setBody(PAGINA_VACUNACIONES));

        // Act
        Response<Pagina<Vacunacion>> r = api.vacunacionesDeMascota(1).execute();

        // Assert
        Vacunacion vacunacion = r.body().getResults().get(0);
        assertEquals("Quíntuple", vacunacion.getVacunaNombre());
        assertEquals("2026-10-13", vacunacion.getProximaDosis());
        assertEquals(Integer.valueOf(0), vacunacion.getDiasParaProximaDosis());
        assertEquals("Dra. Gómez", vacunacion.getVeterinarioNombre());
    }

    @Test
    public void turnosDeMascota_respuesta500_seManejaSinRomperse() throws Exception {
        // Arrange: el backend falla y responde con la página HTML de Django
        servidor.enqueue(new MockResponse().setResponseCode(500)
                .setHeader("Content-Type", "text/html")
                .setBody(ERROR_500_DE_DJANGO));

        // Act
        Response<Pagina<Turno>> r = api.turnosDeMascota(1).execute();
        String mensaje = ApiError.mensaje(r);

        // Assert
        assertFalse(r.isSuccessful());
        assertNull(r.body());
        assertEquals("Error del servidor. Intentá más tarde.", mensaje);
        assertEquals(1, servidor.getRequestCount());   // un 500 no dispara la renovación del token
    }

    @Test
    public void vacunacionesDeMascota_respuesta500SinCuerpo_muestraMensajeGenerico() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setResponseCode(500));

        // Act
        Response<Pagina<Vacunacion>> r = api.vacunacionesDeMascota(1).execute();

        // Assert
        assertFalse(r.isSuccessful());
        assertEquals("Error del servidor. Intentá más tarde.", ApiError.mensaje(r));
    }
}
