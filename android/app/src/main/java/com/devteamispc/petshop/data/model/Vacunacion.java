package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/** Aplicación concreta de una vacuna. Sólo el veterinario la registra. */
public class Vacunacion {

    private int id;
    private int mascota;

    @SerializedName("mascota_nombre")
    private String mascotaNombre;

    private int vacuna;

    @SerializedName("vacuna_nombre")
    private String vacunaNombre;

    @SerializedName("fecha_aplicacion")
    private String fechaAplicacion;

    @SerializedName("proxima_dosis")
    private String proximaDosis;

    /** Negativo si la dosis está vencida. null si no hay próxima dosis. */
    @SerializedName("dias_para_proxima_dosis")
    private Integer diasParaProximaDosis;

    private Integer veterinario;

    @SerializedName("veterinario_nombre")
    private String veterinarioNombre;

    public int getId() { return id; }
    public int getMascota() { return mascota; }
    public String getMascotaNombre() { return mascotaNombre; }
    public int getVacuna() { return vacuna; }
    public String getVacunaNombre() { return vacunaNombre; }
    public String getFechaAplicacion() { return fechaAplicacion; }
    public String getProximaDosis() { return proximaDosis; }
    public Integer getDiasParaProximaDosis() { return diasParaProximaDosis; }
    public Integer getVeterinario() { return veterinario; }
    public String getVeterinarioNombre() { return veterinarioNombre; }
}
