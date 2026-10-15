package com.devteamispc.petshop.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

/**
 * AUT-UNIT-01 · Validadores de registro, inicio de sesión y contacto (US01, US02, US09).
 *
 * Diseño por clases de equivalencia y valores límite: un valor válido, uno
 * vacío, uno en el límite y uno fuera de formato. Patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.util.ValidacionesAccesoTest"
 */
public class ValidacionesAccesoTest {

    // ------------------------------------------------------------ obligatorio

    @Test
    public void obligatorio_soloEspacios_esInvalido() {
        // Arrange
        String texto = "   ";

        // Act
        boolean valido = Validaciones.obligatorio(texto);

        // Assert
        assertFalse(valido);
    }

    @Test
    public void obligatorio_conTexto_esValido() {
        // Arrange
        String texto = "Rocco";

        // Act
        boolean valido = Validaciones.obligatorio(texto);

        // Assert
        assertTrue(valido);
    }

    // ------------------------------------------------------------------ email

    @Test
    public void email_conFormatoCorrecto_esValido() {
        // Arrange
        String email = "cliente@petshop.test";

        // Act
        boolean valido = Validaciones.emailValido(email);

        // Assert
        assertTrue(valido);
    }

    @Test
    public void email_sinArroba_esInvalido() {
        // Arrange
        String email = "cliente.petshop.test";

        // Act
        boolean valido = Validaciones.emailValido(email);

        // Assert
        assertFalse(valido);
    }

    @Test
    public void email_sinDominio_esInvalido() {
        // Arrange
        String email = "cliente@petshop";

        // Act
        boolean valido = Validaciones.emailValido(email);

        // Assert
        assertFalse(valido);
    }

    // ----------------------------------------------- contraseña: valores límite

    @Test
    public void password_de8Caracteres_esInvalida() {
        // Arrange: 8 caracteres, uno menos que el mínimo (RNF15: "más de 8")
        String password = "Petsh0p!";

        // Act
        List<String> faltantes = Validaciones.faltantesPassword(password);

        // Assert
        assertTrue(faltantes.contains("más de 8 caracteres"));
    }

    @Test
    public void password_de9CaracteresConTodasLasReglas_esValida() {
        // Arrange: justo en el límite
        String password = "Petsho0p!";

        // Act
        List<String> faltantes = Validaciones.faltantesPassword(password);

        // Assert
        assertTrue(faltantes.isEmpty());
    }

    @Test
    public void password_deLaCuentaDePrueba_noTieneError() {
        // Arrange
        String password = "Petshop2026!";

        // Act
        String error = Validaciones.errorPassword(password);

        // Assert
        assertNull(error);
    }

    @Test
    public void password_sinCaracterEspecial_loInforma() {
        // Arrange
        String password = "Petshop2026";

        // Act
        List<String> faltantes = Validaciones.faltantesPassword(password);

        // Assert
        assertTrue(faltantes.contains("un carácter especial"));
    }

    @Test
    public void password_vacia_faltanLasCincoReglas() {
        // Arrange
        String password = "";

        // Act
        List<String> faltantes = Validaciones.faltantesPassword(password);

        // Assert
        assertEquals(5, faltantes.size());
    }

    @Test
    public void errorPassword_conVariasFaltas_armaUnTextoLegible() {
        // Arrange
        String password = "petshop2026";

        // Act
        String error = Validaciones.errorPassword(password);

        // Assert
        assertEquals("Falta: una mayúscula y un carácter especial.", error);
    }

    @Test
    public void mayusculaYMinuscula_soloMayusculas_noAlcanza() {
        // Arrange
        String soloMayusculas = "PETSHOP";

        // Act
        boolean cumple = Validaciones.tieneMayusculaYMinuscula(soloMayusculas);

        // Assert
        assertFalse(cumple);
    }

    // ------------------------------------------- mensaje de contacto: límites

    @Test
    public void mensaje_de9Caracteres_esInvalido() {
        // Arrange: uno menos que el mínimo (10)
        String mensaje = "123456789";

        // Act
        boolean valido = Validaciones.mensajeValido(mensaje);

        // Assert
        assertFalse(valido);
    }

    @Test
    public void mensaje_de10Caracteres_esValido() {
        // Arrange: justo en el mínimo
        String mensaje = "1234567890";

        // Act
        boolean valido = Validaciones.mensajeValido(mensaje);

        // Assert
        assertTrue(valido);
    }

    @Test
    public void mensaje_de501Caracteres_esInvalido() {
        // Arrange: uno más que el máximo (500)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 501; i++) sb.append('a');

        // Act
        boolean valido = Validaciones.mensajeValido(sb.toString());

        // Assert
        assertFalse(valido);
    }

    @Test
    public void mensaje_conEspaciosEnLosBordes_noLosCuenta() {
        // Arrange: 4 letras útiles rodeadas de espacios
        String mensaje = "   hola     ";

        // Act
        boolean valido = Validaciones.mensajeValido(mensaje);

        // Assert
        assertFalse(valido);
    }
}
