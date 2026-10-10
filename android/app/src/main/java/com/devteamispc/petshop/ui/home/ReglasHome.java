package com.devteamispc.petshop.ui.home;

import com.devteamispc.petshop.data.model.Usuario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Qué accesos ve cada rol en el inicio, en el orden del mockup. Sin
 * dependencias de Android: se prueba con tests unitarios (ReglasHomeTest).
 *
 * Mostrar u ocultar un acceso es presentación: si alguien igual llega a una
 * pantalla que no le corresponde, la pantalla se cierra y la API responde 403.
 */
public final class ReglasHome {

    public enum Acceso {
        MASCOTAS, AGENDA, PEDIR_TURNO, REGISTRAR_VACUNA, VACUNAS, USUARIOS, SERVICIOS, CONSULTAS, CONTACTO, PERFIL
    }

    private ReglasHome() { }

    public static List<Acceso> accesos(String rol) {
        if (Usuario.ROL_ADMIN.equals(rol)) {
            return lista(Acceso.USUARIOS, Acceso.SERVICIOS, Acceso.AGENDA, Acceso.CONSULTAS, Acceso.MASCOTAS, Acceso.PERFIL);
        }
        if (Usuario.ROL_VETERINARIO.equals(rol)) {
            return lista(Acceso.REGISTRAR_VACUNA, Acceso.AGENDA, Acceso.MASCOTAS, Acceso.VACUNAS, Acceso.PERFIL);
        }
        if (Usuario.ROL_CLIENTE.equals(rol)) {
            return lista(Acceso.MASCOTAS, Acceso.AGENDA, Acceso.PEDIR_TURNO, Acceso.PERFIL, Acceso.CONTACTO);
        }
        // Rol desconocido o sesión rota: no se ofrece nada.
        return Collections.emptyList();
    }

    private static List<Acceso> lista(Acceso... accesos) {
        return new ArrayList<>(Arrays.asList(accesos));
    }
}
