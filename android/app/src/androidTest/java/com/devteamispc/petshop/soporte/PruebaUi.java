package com.devteamispc.petshop.soporte;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.accessibility.AccessibilityChecks;

import com.devteamispc.petshop.data.api.ApiClient;
import com.devteamispc.petshop.data.local.SessionManager;
import com.devteamispc.petshop.data.model.Usuario;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.net.InetAddress;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Base de los flujos de Espresso (AUT-UI).
 *
 *   - Levanta un servidor simulado en el propio teléfono y apunta la app ahí:
 *     los tests no dependen de internet, de Render ni de los datos del backend.
 *   - Activa los chequeos de accesibilidad automáticos (Accessibility Test
 *     Framework): en cada click o escritura se revisa TODA la pantalla
 *     (tamaño táctil, contraste, etiquetas). Si encuentra un error, el test falla.
 *   - Arranca cada test sin sesión, salvo que el test inicie una.
 */
public abstract class PruebaUi {

    protected MockWebServer servidor;

    @BeforeClass
    public static void activarAccesibilidad() {
        AccessibilityChecks.enable().setRunChecksFromRootView(true);
    }

    @Before
    public void prepararServidor() throws Exception {
        servidor = new MockWebServer();
        servidor.setDispatcher(rutas());
        // "localhost": el network_security_config sólo permite HTTP sin cifrar ahí.
        servidor.start(InetAddress.getByName("localhost"), 0);
        ApiClient.usarServidorDePrueba(servidor.url("/api/").toString());
        sesion().cerrarSesion();
    }

    @After
    public void cerrarServidor() throws Exception {
        servidor.shutdown();
        sesion().cerrarSesion();
    }

    /** Qué responde el servidor simulado en cada ruta. */
    protected abstract Dispatcher rutas();

    protected static Context contexto() {
        return ApplicationProvider.getApplicationContext();
    }

    protected static SessionManager sesion() {
        return SessionManager.get(contexto());
    }

    /** Deja guardada una sesión de cliente, como si ya se hubiera logueado. */
    protected static void iniciarSesionComoCliente() {
        Usuario usuario = new Gson().fromJson(
                "{\"id\":3,\"nombre\":\"Juan Pérez\",\"email\":\"cliente@petshop.test\",\"rol\":\"cliente\"}",
                Usuario.class);
        sesion().guardarSesion("access-de-prueba", "refresh-de-prueba", usuario);
    }
}
