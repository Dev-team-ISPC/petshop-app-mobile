package com.devteamispc.petshop.data.model;

/**
 * Cuerpo del PATCH /turnos/{id}/ para mover el estado.
 * El cliente sólo puede mandar "cancelado"; el resto es del veterinario.
 */
public class CambioEstado {

    private final String estado;

    public CambioEstado(String estado) { this.estado = estado; }
}
