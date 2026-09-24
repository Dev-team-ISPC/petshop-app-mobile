package com.devteamispc.petshop.data.model;

/** Catálogo de vacunas. Sólo el veterinario lo modifica. */
public class Vacuna {

    private int id;
    private String nombre;
    private String descripcion;
    private String frecuencia;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getFrecuencia() { return frecuencia; }

    @Override
    public String toString() { return nombre; }
}
