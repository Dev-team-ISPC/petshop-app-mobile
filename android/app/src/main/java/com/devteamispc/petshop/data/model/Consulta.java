package com.devteamispc.petshop.data.model;

import com.google.gson.annotations.SerializedName;

/** Mensaje del formulario de contacto. El envío es público. */
public class Consulta {

    private int id;
    private String nombre;
    private String email;
    private String mensaje;
    private boolean leida;

    @SerializedName("creado_en")
    private String creadoEn;

    public Consulta() { }

    public Consulta(String nombre, String email, String mensaje) {
        this.nombre = nombre;
        this.email = email;
        this.mensaje = mensaje;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getMensaje() { return mensaje; }
    public boolean isLeida() { return leida; }
    public String getCreadoEn() { return creadoEn; }
}
