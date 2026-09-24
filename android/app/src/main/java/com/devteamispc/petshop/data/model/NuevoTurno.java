package com.devteamispc.petshop.data.model;

/** Cuerpo del POST /turnos/. La fecha va en ISO 8601 UTC. */
public class NuevoTurno {

    private final int mascota;
    private final int servicio;
    private final String fecha;
    private final String observaciones;

    public NuevoTurno(int mascota, int servicio, String fecha, String observaciones) {
        this.mascota = mascota;
        this.servicio = servicio;
        this.fecha = fecha;
        this.observaciones = observaciones;
    }
}
