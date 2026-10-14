package com.devteamispc.petshop.ui.home;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.devteamispc.petshop.data.model.Usuario;

import org.junit.Test;

import java.util.List;

/**
 * AUT-UNIT-06 · Accesos de la pantalla de inicio según el rol (US03, RF03).
 *
 * Una sola Activity sirve a los tres roles: estas reglas deciden qué accesos
 * ve cada uno. La autorización real la hace el backend; esto evita mostrar
 * acciones que el servidor después rechazaría. Patrón AAA.
 *
 * ./gradlew testDebugUnitTest --tests "com.devteamispc.petshop.ui.home.ReglasHomeTest"
 */
public class ReglasHomeTest {

    @Test
    public void admin_veLaAdministracion() {
        // Arrange
        String rol = Usuario.ROL_ADMIN;

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertTrue(accesos.contains(ReglasHome.Acceso.USUARIOS));
        assertTrue(accesos.contains(ReglasHome.Acceso.SERVICIOS));
        assertTrue(accesos.contains(ReglasHome.Acceso.CONSULTAS));
    }

    @Test
    public void admin_noPideTurnosNiRegistraVacunas() {
        // Arrange
        String rol = Usuario.ROL_ADMIN;

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertFalse(accesos.contains(ReglasHome.Acceso.PEDIR_TURNO));
        assertFalse(accesos.contains(ReglasHome.Acceso.REGISTRAR_VACUNA));
        assertFalse(accesos.contains(ReglasHome.Acceso.VACUNAS));
    }

    @Test
    public void cliente_noVeLaAdministracion() {
        // Arrange
        String rol = Usuario.ROL_CLIENTE;

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertFalse(accesos.contains(ReglasHome.Acceso.USUARIOS));
        assertFalse(accesos.contains(ReglasHome.Acceso.SERVICIOS));
        assertFalse(accesos.contains(ReglasHome.Acceso.VACUNAS));
    }

    @Test
    public void cliente_pideTurnoYTieneContacto() {
        // Arrange
        String rol = Usuario.ROL_CLIENTE;

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertTrue(accesos.contains(ReglasHome.Acceso.PEDIR_TURNO));
        assertTrue(accesos.contains(ReglasHome.Acceso.CONTACTO));
    }

    @Test
    public void cliente_elPrimerAccesoEsMascotas() {
        // Arrange
        String rol = Usuario.ROL_CLIENTE;

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertEquals(ReglasHome.Acceso.MASCOTAS, accesos.get(0));
    }

    @Test
    public void veterinario_registraVacunasPeroNoAdministra() {
        // Arrange
        String rol = Usuario.ROL_VETERINARIO;

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertTrue(accesos.contains(ReglasHome.Acceso.REGISTRAR_VACUNA));
        assertTrue(accesos.contains(ReglasHome.Acceso.VACUNAS));
        assertFalse(accesos.contains(ReglasHome.Acceso.USUARIOS));
        assertFalse(accesos.contains(ReglasHome.Acceso.CONSULTAS));
        assertFalse(accesos.contains(ReglasHome.Acceso.PEDIR_TURNO));
    }

    @Test
    public void cualquierRol_tienePerfil() {
        // Arrange
        String[] roles = {Usuario.ROL_CLIENTE, Usuario.ROL_VETERINARIO, Usuario.ROL_ADMIN};

        for (String rol : roles) {
            // Act
            List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

            // Assert
            assertTrue("Sin perfil para " + rol, accesos.contains(ReglasHome.Acceso.PERFIL));
        }
    }

    @Test
    public void rolDesconocido_noVeNada() {
        // Arrange
        String rol = "otro";

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertTrue(accesos.isEmpty());
    }

    @Test
    public void rolNulo_noVeNada() {
        // Arrange
        String rol = null;

        // Act
        List<ReglasHome.Acceso> accesos = ReglasHome.accesos(rol);

        // Assert
        assertTrue(accesos.isEmpty());
    }
}
