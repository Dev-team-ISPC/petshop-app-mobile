package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/** Servicio que presta la veterinaria. Un turno se pide para un servicio. */
public class Servicio {

    private int id;
    private String nombre;
    private String descripcion;

    @SerializedName("duracion_minutos")
    private int duracionMinutos;

    private boolean activo;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public boolean isActivo() { return activo; }

    @Override
    public String toString() { return nombre; }
}
