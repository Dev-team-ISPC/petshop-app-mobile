package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * GET /resumen/. Los campos que llegan dependen del rol: los que no
 * corresponden vienen en null, por eso son Integer y no int.
 */
public class Resumen {

    private String rol;
    private String nombre;

    @SerializedName("total_mascotas")
    private Integer totalMascotas;

    @SerializedName("turnos_pendientes")
    private Integer turnosPendientes;

    @SerializedName("turnos_hoy")
    private Integer turnosHoy;

    @SerializedName("total_usuarios")
    private Integer totalUsuarios;

    @SerializedName("total_turnos")
    private Integer totalTurnos;

    @SerializedName("total_servicios")
    private Integer totalServicios;

    @SerializedName("consultas_sin_leer")
    private Integer consultasSinLeer;

    @SerializedName("proxima_dosis")
    private Agenda.ProximaDosis proximaDosis;

    public String getRol() { return rol; }
    public String getNombre() { return nombre; }
    public Integer getTotalMascotas() { return totalMascotas; }
    public Integer getTurnosPendientes() { return turnosPendientes; }
    public Integer getTurnosHoy() { return turnosHoy; }
    public Integer getTotalUsuarios() { return totalUsuarios; }
    public Integer getTotalTurnos() { return totalTurnos; }
    public Integer getTotalServicios() { return totalServicios; }
    public Integer getConsultasSinLeer() { return consultasSinLeer; }
    public Agenda.ProximaDosis getProximaDosis() { return proximaDosis; }
}
