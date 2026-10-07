package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * El peso viaja como String porque DRF serializa los decimales así.
 * No hay campo dueño al crear: lo asigna el servidor si quien crea es cliente.
 */
public class Mascota {

    private int id;
    private String nombre;
    private String especie;

    @SerializedName("especie_display")
    private String especieDisplay;

    private String raza;
    private String peso;

    @SerializedName("fecha_nacimiento")
    private String fechaNacimiento;

    @SerializedName("edad_anios")
    private int edadAnios;

    private Integer dueno;

    @SerializedName("dueno_nombre")
    private String duenoNombre;

    public Mascota() { }

    public Mascota(String nombre, String especie, String raza, String peso, String fechaNacimiento) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.peso = peso;
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecie() { return especie; }
    public String getEspecieDisplay() { return especieDisplay; }
    public String getRaza() { return raza; }
    public String getPeso() { return peso; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public int getEdadAnios() { return edadAnios; }
    public Integer getDueno() { return dueno; }
    public String getDuenoNombre() { return duenoNombre; }

    public void setDueno(Integer dueno) { this.dueno = dueno; }

    /** Inicial para el avatar de la lista. */
    public String getInicial() {
        return (nombre == null || nombre.isEmpty()) ? "?" : nombre.substring(0, 1).toUpperCase();
    }
}
