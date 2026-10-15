package com.devteamispc.petshop.flujos;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.devteamispc.petshop.pantallas.PantallaHome;
import com.devteamispc.petshop.pantallas.PantallaLogin;
import com.devteamispc.petshop.soporte.PruebaUi;
import com.devteamispc.petshop.soporte.Respuestas;
import com.devteamispc.petshop.ui.auth.LoginActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * AUT-UI-01 · Inicio de sesión (US02, RF02).
 *
 * Con credenciales inválidas se muestra un mensaje genérico (no dice si falló
 * el email o la contraseña); con las correctas se entra al inicio del cliente.
 * Se lanza el Login directamente, sin pasar por el splash. El servidor es
 * simulado y los chequeos de accesibilidad están activos (PruebaUi). Patrón AAA.
 *
 * ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.devteamispc.petshop.flujos.FlujoLoginTest
 */
@RunWith(AndroidJUnit4.class)
public class FlujoLoginTest extends PruebaUi {

    @Override
    protected Dispatcher rutas() {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest pedido) {
                String ruta = pedido.getPath();
                if (ruta.startsWith("/api/auth/login/")) {
                    boolean correcta = pedido.getBody().readUtf8().contains("Petshop2026!");
                    return correcta
                            ? Respuestas.json(200, Respuestas.LOGIN_CLIENTE)
                            : Respuestas.json(401, "{\"detail\":\"No active account found with the given credentials\"}");
                }
                if (ruta.startsWith("/api/resumen/")) return Respuestas.json(200, Respuestas.RESUMEN_CLIENTE);
                return Respuestas.noEncontrado();
            }
        };
    }

    @Test
    public void credencialesInvalidas_muestraMensajeGenerico() {
        // Arrange
        try (ActivityScenario<LoginActivity> ignorada = ActivityScenario.launch(LoginActivity.class)) {
            PantallaLogin login = new PantallaLogin();

            // Act
            login.ingresar("cliente@petshop.test", "Incorrecta1!");

            // Assert
            login.verError("Email o contraseña incorrectos.");
        }
    }

    @Test
    public void credencialesCorrectas_entraAlInicioDelCliente() {
        // Arrange
        try (ActivityScenario<LoginActivity> ignorada = ActivityScenario.launch(LoginActivity.class)) {
            PantallaLogin login = new PantallaLogin();

            // Act
            login.ingresar("cliente@petshop.test", "Petshop2026!");

            // Assert
            new PantallaHome()
                    .verSaludo("Juan Pérez")
                    .verAcceso("Pedir turno");
        }
    }
}
