package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * El cliente crea el turno como pendiente y puede cancelarlo.
 * Confirmar y completar son del veterinario.
 */
public class Turno {

    public static final String PENDIENTE = "pendiente";
    public static final String CONFIRMADO = "confirmado";
    public static final String CANCELADO = "cancelado";
    public static final String COMPLETADO = "completado";

    private int id;
    private int mascota;

    @SerializedName("mascota_nombre")
    private String mascotaNombre;

    private Integer dueno;

    @SerializedName("dueno_nombre")
    private String duenoNombre;

    private int servicio;

    @SerializedName("servicio_nombre")
    private String servicioNombre;

    private Integer veterinario;

    @SerializedName("veterinario_nombre")
    private String veterinarioNombre;

    /** ISO 8601 UTC. */
    private String fecha;

    /** Negativo si el turno ya pasó. */
    @SerializedName("dias_restantes")
    private int diasRestantes;

    private String estado;

    @SerializedName("estado_display")
    private String estadoDisplay;

    private String observaciones;

    public int getId() { return id; }
    public int getMascota() { return mascota; }
    public String getMascotaNombre() { return mascotaNombre; }
    public Integer getDueno() { return dueno; }
    public String getDuenoNombre() { return duenoNombre; }
    public int getServicio() { return servicio; }
    public String getServicioNombre() { return servicioNombre; }
    public Integer getVeterinario() { return veterinario; }
    public String getVeterinarioNombre() { return veterinarioNombre; }
    public String getFecha() { return fecha; }
    public int getDiasRestantes() { return diasRestantes; }
    public String getEstado() { return estado; }
    public String getEstadoDisplay() { return estadoDisplay; }
    public String getObservaciones() { return observaciones; }

    public boolean estaPendiente() { return PENDIENTE.equals(estado); }
    public boolean estaCerrado() { return CANCELADO.equals(estado) || COMPLETADO.equals(estado); }
}
