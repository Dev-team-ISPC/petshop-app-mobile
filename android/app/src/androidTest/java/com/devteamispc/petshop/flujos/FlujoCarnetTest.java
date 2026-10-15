package com.devteamispc.petshop.flujos;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.devteamispc.petshop.pantallas.PantallaCarnet;
import com.devteamispc.petshop.pantallas.PantallaMascotas;
import com.devteamispc.petshop.soporte.PruebaUi;
import com.devteamispc.petshop.soporte.Respuestas;
import com.devteamispc.petshop.ui.mascota.MascotasActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * AUT-UI-03 · Mis mascotas → Carnet (US04, US05).
 *
 * Con sesión de cliente: la lista muestra su mascota y al tocarla se abre el
 * carnet con sus vacunas y turnos (traspaso de información entre Activities
 * por extra del Intent). Verifica también lo que el cliente NO puede hacer:
 * no ve el botón para registrar vacunas, sólo el de pedir turno.
 * Servidor simulado y chequeos de accesibilidad activos (PruebaUi). Patrón AAA.
 *
 * ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.devteamispc.petshop.flujos.FlujoCarnetTest
 */
@RunWith(AndroidJUnit4.class)
public class FlujoCarnetTest extends PruebaUi {

    @Override
    protected Dispatcher rutas() {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest pedido) {
                String ruta = pedido.getPath();
                if (ruta.startsWith("/api/mascotas/1/vacunaciones/")) return Respuestas.json(200, Respuestas.PAGINA_VACUNACIONES);
                if (ruta.startsWith("/api/mascotas/1/turnos/")) return Respuestas.json(200, Respuestas.PAGINA_TURNOS);
                if (ruta.startsWith("/api/mascotas/1/")) return Respuestas.json(200, Respuestas.MASCOTA_ROCCO);
                if (ruta.startsWith("/api/mascotas/")) return Respuestas.json(200, Respuestas.PAGINA_MASCOTAS);
                return Respuestas.noEncontrado();
            }
        };
    }

    @Before
    public void conSesionDeCliente() {
        iniciarSesionComoCliente();
    }

    @Test
    public void tocarUnaMascota_abreSuCarnetConVacunas_ySinBotonParaRegistrarlas() {
        // Arrange
        try (ActivityScenario<MascotasActivity> ignorada = ActivityScenario.launch(MascotasActivity.class)) {
            PantallaMascotas mascotas = new PantallaMascotas().verMascota("Rocco");

            // Act
            PantallaCarnet carnet = mascotas.abrir("Rocco");

            // Assert
            carnet.verVacuna("Quíntuple")
                  .verSinBotonAgregar();
        }
    }

    @Test
    public void pestanaTurnos_muestraSusTurnos_yPermitePedirOtro() {
        // Arrange
        try (ActivityScenario<MascotasActivity> ignorada = ActivityScenario.launch(MascotasActivity.class)) {
            PantallaCarnet carnet = new PantallaMascotas().verMascota("Rocco").abrir("Rocco").verVacuna("Quíntuple");

            // Act
            carnet.irATurnos();

            // Assert
            carnet.verTurno("Consulta general")
                  .verBotonAgregar();
        }
    }
}
