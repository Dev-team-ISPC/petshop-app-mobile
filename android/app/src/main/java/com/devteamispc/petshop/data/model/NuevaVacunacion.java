package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Cuerpo del POST /vacunaciones/. Las fechas van en yyyy-MM-dd.
 * El veterinario queda asignado solo: no hace falta mandarlo.
 */
public class NuevaVacunacion {

    private final int mascota;
    private final int vacuna;

    @SerializedName("fecha_aplicacion")
    private final String fechaAplicacion;

    @SerializedName("proxima_dosis")
    private final String proximaDosis;

    public NuevaVacunacion(int mascota, int vacuna, String fechaAplicacion, String proximaDosis) {
        this.mascota = mascota;
        this.vacuna = vacuna;
        this.fechaAplicacion = fechaAplicacion;
        this.proximaDosis = proximaDosis;
    }
}
