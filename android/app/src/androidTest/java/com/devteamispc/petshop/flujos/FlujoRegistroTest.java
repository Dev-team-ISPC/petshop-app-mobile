package com.devteamispc.petshop.flujos;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.devteamispc.petshop.pantallas.PantallaLogin;
import com.devteamispc.petshop.pantallas.PantallaRegistro;
import com.devteamispc.petshop.soporte.PruebaUi;
import com.devteamispc.petshop.soporte.Respuestas;
import com.devteamispc.petshop.ui.auth.LoginActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * AUT-UI-02 · Registro (US01, RF01, RF15) y navegación Login → Registro.
 *
 * Es el flujo que marcó la devolución de la Evidencia 2: "Crear cuenta" tenía
 * que abrir el Registro y volvía al Login. Además se verifica que las reglas
 * de la contraseña se marquen mientras se escribe y que no se pueda registrar
 * sin aceptar los términos. No necesita red: todo se valida en el teléfono.
 * Chequeos de accesibilidad activos (PruebaUi). Patrón AAA.
 *
 * ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.devteamispc.petshop.flujos.FlujoRegistroTest
 */
@RunWith(AndroidJUnit4.class)
public class FlujoRegistroTest extends PruebaUi {

    @Override
    protected Dispatcher rutas() {
        return new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest pedido) {
                return Respuestas.noEncontrado();
            }
        };
    }

    @Test
    public void desdeElLogin_crearCuenta_abreElRegistro() {
        // Arrange
        try (ActivityScenario<LoginActivity> ignorada = ActivityScenario.launch(LoginActivity.class)) {
            PantallaLogin login = new PantallaLogin();

            // Act
            PantallaRegistro registro = login.irARegistro();

            // Assert
            registro.verPantalla();
        }
    }

    @Test
    public void passwordSinCaracterEspecial_marcaSoloEsaReglaComoFaltante() {
        // Arrange
        try (ActivityScenario<LoginActivity> ignorada = ActivityScenario.launch(LoginActivity.class)) {
            PantallaRegistro registro = new PantallaLogin().irARegistro().verPantalla();

            // Act
            registro.completar("Ana López", "ana@mail.com", "Petshop2026");

            // Assert
            registro.verReglaLargo(true)
                    .verReglaMayusculas(true)
                    .verReglaNumero(true)
                    .verReglaEspecial(false);
        }
    }

    @Test
    public void sinAceptarLosTerminos_noRegistraYLoInforma() {
        // Arrange
        try (ActivityScenario<LoginActivity> ignorada = ActivityScenario.launch(LoginActivity.class)) {
            PantallaRegistro registro = new PantallaLogin().irARegistro().verPantalla()
                    .completar("Ana López", "ana@mail.com", "Petshop2026!");

            // Act
            registro.registrarme();

            // Assert
            registro.verError("Para crear la cuenta tenés que aceptar los términos y la política de privacidad.");
        }
    }
}
