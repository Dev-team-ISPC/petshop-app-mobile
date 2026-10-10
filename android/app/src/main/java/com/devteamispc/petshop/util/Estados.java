package com.devteamispc.petshop.util;

import com.devteamispc.petshop.data.model.Turno;

/**
 * Reglas de presentación de turnos y dosis, sin dependencias de Android:
 * se prueban con tests unitarios en la JVM (EstadosTest).
 *
 * Las usan el Carnet y la Agenda para decidir el color de cada badge y qué
 * botones ve cada rol. La autorización real la hace el servidor: esto sólo
 * evita mostrar acciones que la API rechazaría.
 */
public final class Estados {

    /** Nivel visual de un badge. El texto siempre acompaña al color (WCAG 1.4.1). */
    public enum Nivel { PELIGRO, ATENCION, OK, NEUTRO }

    /** Días desde los que una dosis próxima pasa a "atención" (mockup: 25 días en ámbar). */
    public static final int DIAS_AVISO_DOSIS = 30;

    private Estados() { }

    /**
     * Vencida: peligro. Dentro de 30 días: atención. Más adelante o sin fecha:
     * neutro (en el mockup, "En 185 días" va en gris).
     */
    public static Nivel nivelDosis(Integer dias) {
        if (dias == null) return Nivel.NEUTRO;
        if (dias < 0) return Nivel.PELIGRO;
        if (dias <= DIAS_AVISO_DOSIS) return Nivel.ATENCION;
        return Nivel.NEUTRO;
    }

    public static Nivel nivelTurno(String estado) {
        if (Turno.PENDIENTE.equals(estado)) return Nivel.ATENCION;
        if (Turno.CONFIRMADO.equals(estado)) return Nivel.OK;
        if (Turno.CANCELADO.equals(estado)) return Nivel.PELIGRO;
        return Nivel.NEUTRO;
    }

    /** Sólo el veterinario confirma, y sólo turnos pendientes. */
    public static boolean puedeConfirmar(boolean esVeterinario, String estado) {
        return esVeterinario && Turno.PENDIENTE.equals(estado);
    }

    /** Sólo el veterinario marca como completado, y sólo un turno confirmado. */
    public static boolean puedeCompletar(boolean esVeterinario, String estado) {
        return esVeterinario && Turno.CONFIRMADO.equals(estado);
    }

    /**
     * El veterinario y el cliente pueden cancelar mientras el turno no esté
     * cerrado. El administrador sólo consulta.
     */
    public static boolean puedeCancelar(boolean esVeterinario, boolean esCliente, String estado) {
        if (!esVeterinario && !esCliente) return false;
        return !Turno.CANCELADO.equals(estado) && !Turno.COMPLETADO.equals(estado);
    }

    /** Texto del estado, para cuando la API no manda estado_display (la agenda). */
    public static String etiquetaEstado(String estado) {
        if (estado == null || estado.isEmpty()) return "";
        return Character.toUpperCase(estado.charAt(0)) + estado.substring(1);
    }
}
