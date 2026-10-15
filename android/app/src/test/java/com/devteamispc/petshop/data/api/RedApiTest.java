package com.devteamispc.petshop.data.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.devteamispc.petshop.data.model.LoginRequest;
import com.devteamispc.petshop.data.model.LoginResponse;
import com.devteamispc.petshop.data.model.Mascota;
import com.devteamispc.petshop.data.model.Pagina;
import com.devteamispc.petshop.data.model.RegistroRequest;
import com.devteamispc.petshop.data.model.Usuario;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Response;

/**
 * AUT-NET-01 y 02 · Autenticación, mascotas y errores contra MockWebServer (US02, US04).
 *
 * Corren en la JVM, sin emulador ni internet: la capa de red real (Retrofit,
 * OkHttp, Gson, AuthInterceptor y TokenAuthenticator) contra un servidor simulado.
 * Verifican lo que ningún test de pantalla ve: que el token viaje donde tiene
 * que viajar, que la renovación automática funcione y que los JSON reales del
 * backend se conviertan bien en objetos. Patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.data.api.RedApiTest"
 */
public class RedApiTest {

    /** Almacén de tokens en memoria: reemplaza a SessionManager en los tests. */
    private static class TokensEnMemoria implements AlmacenTokens {
        String access;
        String refresh;
        boolean cerrada = false;

        TokensEnMemoria(String access, String refresh) {
            this.access = access;
            this.refresh = refresh;
        }

        @Override public String getAccess() { return access; }
        @Override public String getRefresh() { return refresh; }
        @Override public void actualizarTokens(String a, String r) { access = a; refresh = r; }
        @Override public void cerrarSesion() { access = null; refresh = null; cerrada = true; }
    }

    private static final String PAGINA_MASCOTAS = "{\"count\":2,\"next\":null,\"previous\":null,\"results\":["
            + "{\"id\":1,\"nombre\":\"Rocco\",\"especie\":\"perro\",\"especie_display\":\"Perro\",\"raza\":\"Labrador\","
            + "\"peso\":\"28.50\",\"fecha_nacimiento\":\"2021-03-10\",\"edad_anios\":5,\"dueno\":3,\"dueno_nombre\":\"Juan Pérez\"},"
            + "{\"id\":2,\"nombre\":\"Mía\",\"especie\":\"gato\",\"especie_display\":\"Gato\",\"raza\":\"Siamés\","
            + "\"peso\":\"4.20\",\"fecha_nacimiento\":\"2022-01-05\",\"edad_anios\":4,\"dueno\":3,\"dueno_nombre\":\"Juan Pérez\"}]}";

    private MockWebServer servidor;
    private TokensEnMemoria tokens;
    private PetshopApi api;

    @Before
    public void preparar() throws Exception {
        servidor = new MockWebServer();
        servidor.start();
        tokens = new TokensEnMemoria("access-viejo", "refresh-viejo");
        api = ApiClient.construirCon(servidor.url("/api/").toString(), tokens);
    }

    @After
    public void cerrar() throws Exception {
        servidor.shutdown();
    }

    private static Map<String, String> sinFiltros() {
        return new HashMap<>();
    }

    @Test
    public void endpointPrivado_llevaElTokenBearer() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setBody(PAGINA_MASCOTAS));

        // Act
        api.mascotas(sinFiltros()).execute();

        // Assert
        RecordedRequest pedido = servidor.takeRequest();
        assertEquals("/api/mascotas/", pedido.getPath());
        assertEquals("Bearer access-viejo", pedido.getHeader("Authorization"));
    }

    @Test
    public void login_esPublico_yNoMandaToken() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setBody(
                "{\"access\":\"a\",\"refresh\":\"r\",\"usuario\":{\"id\":3,\"nombre\":\"Juan\",\"rol\":\"cliente\"}}"));
        LoginRequest credenciales = new LoginRequest("cliente@petshop.test", "Petshop2026!");

        // Act
        Response<LoginResponse> r = api.login(credenciales).execute();

        // Assert
        assertNull(servidor.takeRequest().getHeader("Authorization"));
        assertEquals("cliente", r.body().getUsuario().getRol());
    }

    @Test
    public void tokenVencido_seRenuevaYSeRepiteElPedido() throws Exception {
        // Arrange: 401 -> refresh exitoso -> la lista
        servidor.enqueue(new MockResponse().setResponseCode(401));
        servidor.enqueue(new MockResponse().setBody("{\"access\":\"access-nuevo\",\"refresh\":\"refresh-nuevo\"}"));
        servidor.enqueue(new MockResponse().setBody(PAGINA_MASCOTAS));

        // Act
        Response<Pagina<Mascota>> r = api.mascotas(sinFiltros()).execute();

        // Assert
        assertTrue(r.isSuccessful());
        assertEquals(3, servidor.getRequestCount());
        servidor.takeRequest();                                   // el 401
        RecordedRequest refresh = servidor.takeRequest();
        assertEquals("/api/auth/refresh/", refresh.getPath());
        assertNull(refresh.getHeader("Authorization"));           // el refresh va sin token
        assertTrue(refresh.getBody().readUtf8().contains("refresh-viejo"));
        assertEquals("Bearer access-nuevo", servidor.takeRequest().getHeader("Authorization"));
        // Rotación: se guardan los dos tokens nuevos.
        assertEquals("access-nuevo", tokens.getAccess());
        assertEquals("refresh-nuevo", tokens.getRefresh());
    }

    @Test
    public void refreshRechazado_cierraLaSesion() throws Exception {
        // Arrange: 401 -> el refresh también es rechazado (rotado o revocado)
        servidor.enqueue(new MockResponse().setResponseCode(401));
        servidor.enqueue(new MockResponse().setResponseCode(401).setBody("{\"detail\":\"Token is blacklisted\"}"));

        // Act
        Response<Pagina<Mascota>> r = api.mascotas(sinFiltros()).execute();

        // Assert
        assertEquals(401, r.code());
        assertTrue(tokens.cerrada);
        assertEquals(2, servidor.getRequestCount());              // no reintenta en bucle
    }

    @Test
    public void paginaDeMascotas_seConvierteConLosCamposDelBackend() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setBody(PAGINA_MASCOTAS));

        // Act
        Pagina<Mascota> pagina = api.mascotas(sinFiltros()).execute().body();

        // Assert
        assertEquals(2, pagina.getCount());
        assertFalse(pagina.hayMas());
        Mascota rocco = pagina.getResults().get(0);
        assertEquals("Rocco", rocco.getNombre());
        assertEquals("Perro", rocco.getEspecieDisplay());
        assertEquals("28.50", rocco.getPeso());
        assertEquals("Juan Pérez", rocco.getDuenoNombre());
    }

    @Test
    public void error400_seLeenTodosLosCamposDeUnaVez() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setResponseCode(400).setBody(
                "{\"email\":[\"Ya existe un usuario con ese email.\"],"
                        + "\"password\":[\"La contraseña debe tener al menos un carácter especial.\"]}"));
        RegistroRequest datos = new RegistroRequest("Ana", "ana@mail.com", "Petshop2026", "", "", true);

        // Act
        Response<Usuario> r = api.registro(datos).execute();
        ErroresDeCampo errores = ErroresDeCampo.de(r);

        // Assert
        assertEquals("Ya existe un usuario con ese email.", errores.campo("email"));
        assertEquals("La contraseña debe tener al menos un carácter especial.", errores.campo("password"));
        assertNull(errores.general());
    }

    @Test
    public void error403_muestraElDetalleDelBackend() throws Exception {
        // Arrange
        servidor.enqueue(new MockResponse().setResponseCode(403).setBody(
                "{\"detail\":\"No tiene permiso para realizar esta acción.\"}"));

        // Act
        Response<Pagina<Mascota>> r = api.mascotas(sinFiltros()).execute();

        // Assert
        assertEquals("No tiene permiso para realizar esta acción.", ApiError.mensaje(r));
    }
}
